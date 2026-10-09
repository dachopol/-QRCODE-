$ErrorActionPreference = "Stop"

if ([string]::IsNullOrWhiteSpace($env:ADB_SERIAL)) {
  throw "ADB_SERIAL is required for RMX3241 physical QA"
}
if ([string]::IsNullOrWhiteSpace($env:TEST_INSTRUMENTATION)) {
  throw "TEST_INSTRUMENTATION is required for RMX3241 physical QA"
}

$adb = (Get-Command adb.exe -ErrorAction Stop).Source
$stdout = Join-Path $env:RUNNER_TEMP "quickqr-camera-smoke.stdout.log"
$stderr = Join-Path $env:RUNNER_TEMP "quickqr-camera-smoke.stderr.log"
$localDump = Join-Path $env:RUNNER_TEMP "quickqr-permission.xml"

Remove-Item $stdout, $stderr, $localDump -Force -ErrorAction SilentlyContinue

$arguments = @(
  "-s", $env:ADB_SERIAL,
  "shell", "am", "instrument",
  "-w", "-r",
  "-e", "class", "com.aistudio.qrgenerator.kmpzqr.PhysicalDeviceSmokeInstrumentedTest",
  $env:TEST_INSTRUMENTATION
)

$startArgs = @{
  FilePath = $adb
  ArgumentList = $arguments
  WorkingDirectory = $PWD.Path
  RedirectStandardOutput = $stdout
  RedirectStandardError = $stderr
  PassThru = $true
  WindowStyle = "Hidden"
}
# CameraX bind/rebind is a hardware/runtime gate, not a permission-dialog timing gate.
# Put CAMERA in a deterministic granted state before instrumentation. The in-test
# permission path and host-side dialog handler remain as a fallback/diagnostic path.
$grantOutput = (& adb -s $env:ADB_SERIAL shell pm grant $env:APP_ID android.permission.CAMERA 2>&1) -join "`n"
$grantExit = $LASTEXITCODE
if ($grantExit -ne 0) {
  Write-Host ("CAMERA pre-grant was not accepted; falling back to runtime dialog handling: " + $grantOutput)
} else {
  $packageDump = (& adb -s $env:ADB_SERIAL shell dumpsys package $env:APP_ID) -join "`n"
  if ($packageDump -notmatch "android\.permission\.CAMERA:\s+granted=true") {
    throw "CAMERA pre-grant command succeeded but dumpsys did not confirm granted=true"
  }
  Write-Host "CAMERA permission deterministically granted for CameraX hardware smoke."
}

& adb -s $env:ADB_SERIAL shell am force-stop $env:APP_ID | Out-Null
Start-Sleep -Seconds 1

$process = Start-Process @startArgs

$deadline = [DateTime]::UtcNow.AddMinutes(3)
$permissionHandled = $false

while (-not $process.HasExited -and [DateTime]::UtcNow -lt $deadline) {
  Start-Sleep -Seconds 2

  try {
    & cmd.exe /d /s /c "adb -s $env:ADB_SERIAL shell uiautomator dump /sdcard/quickqr_permission.xml >nul 2>nul" | Out-Null
    $xmlText = (& cmd.exe /d /s /c "adb -s $env:ADB_SERIAL exec-out cat /sdcard/quickqr_permission.xml 2>nul") -join "`n"

    if (-not [string]::IsNullOrWhiteSpace($xmlText)) {
      [xml]$xml = $xmlText
      $candidate = @(
        $xml.SelectNodes("//node") | Where-Object {
          $resourceId = $_.GetAttribute("resource-id")
          $resourceId -match "permission_allow_foreground_only_button|permission_allow_one_time_button|permission_allow_button"
        }
      ) | Select-Object -First 1

      if ($candidate) {
        $bounds = $candidate.GetAttribute("bounds")
        if ($bounds -match "^\[(\d+),(\d+)\]\[(\d+),(\d+)\]$") {
          $x = [int](([int]$Matches[1] + [int]$Matches[3]) / 2)
          $y = [int](([int]$Matches[2] + [int]$Matches[4]) / 2)
          & adb -s $env:ADB_SERIAL shell input tap $x $y | Out-Null
          $permissionHandled = $true
          Write-Host ("Accepted Android camera permission dialog at " + $x + "," + $y)
          Start-Sleep -Seconds 1
        }
      }
    }
  } catch {
    Write-Host ("Permission-dialog probe retry: " + $_.Exception.Message)
  }

  $process.Refresh()
}

if (-not $process.HasExited) {
  Stop-Process -Id $process.Id -Force -ErrorAction SilentlyContinue
  throw "RMX3241 CameraX smoke exceeded 3-minute bound"
}

$process.WaitForExit()

$stdoutText = ""
$stderrText = ""
if (Test-Path -LiteralPath $stdout) {
  $stdoutText = (Get-Content -LiteralPath $stdout) -join "`n"
  $stdoutText | Out-Host
}
if (Test-Path -LiteralPath $stderr) {
  $stderrText = (Get-Content -LiteralPath $stderr) -join "`n"
  $stderrText | Out-Host
}

$combined = ($stdoutText + "`n" + $stderrText).Trim()
$reportedSuccess =
  $combined -match "(?m)^INSTRUMENTATION_STATUS_CODE:\s*0\s*$" -and
  $combined -match "(?m)^INSTRUMENTATION_CODE:\s*-1\s*$"
$reportedFailure =
  $combined -match "FAILURES!!!|INSTRUMENTATION_FAILED|Process crashed|INSTRUMENTATION_STATUS:\s*stack="

$exitCodeKnown = $null -ne $process.ExitCode
if (
  ($exitCodeKnown -and $process.ExitCode -ne 0) -or
  -not $reportedSuccess -or
  $reportedFailure
) {
  $exitLabel = if ($exitCodeKnown) { [string]$process.ExitCode } else { "unavailable" }
  throw ("RMX3241 CameraX smoke failed; exit=" + $exitLabel)
}

Write-Host ("RMX3241 CameraX bind/rebind smoke passed. Permission dialog handled=" + $permissionHandled)
