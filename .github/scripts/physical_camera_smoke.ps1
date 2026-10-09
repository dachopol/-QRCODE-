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

$evidenceDir = Join-Path $PWD "physical-evidence"
New-Item -ItemType Directory -Force -Path $evidenceDir | Out-Null

function Save-CameraDiagnostics {
  $previousPreference = $ErrorActionPreference
  $ErrorActionPreference = "Continue"
  try {
    if (Test-Path -LiteralPath $stdout) {
      Copy-Item -LiteralPath $stdout -Destination (Join-Path $evidenceDir "camera-smoke.stdout.log") -Force
    }
    if (Test-Path -LiteralPath $stderr) {
      Copy-Item -LiteralPath $stderr -Destination (Join-Path $evidenceDir "camera-smoke.stderr.log") -Force
    }

    & adb -s $env:ADB_SERIAL shell screencap -p /sdcard/quickqr_camera_failure.png | Out-Null
    & adb -s $env:ADB_SERIAL pull /sdcard/quickqr_camera_failure.png (Join-Path $evidenceDir "camera-failure.png") | Out-Null

    & cmd.exe /d /s /c "adb -s $env:ADB_SERIAL shell uiautomator dump /sdcard/quickqr_camera_failure.xml >nul 2>nul" | Out-Null
    & adb -s $env:ADB_SERIAL pull /sdcard/quickqr_camera_failure.xml (Join-Path $evidenceDir "camera-failure.xml") | Out-Null

    $packageDump = (& adb -s $env:ADB_SERIAL shell dumpsys package $env:APP_ID 2>&1) -join "`n"
    [IO.File]::WriteAllText(
      (Join-Path $evidenceDir "camera-permission-state.txt"),
      $packageDump,
      (New-Object Text.UTF8Encoding($false))
    )
  } finally {
    $ErrorActionPreference = $previousPreference
  }
}

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
  Save-CameraDiagnostics
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
  Save-CameraDiagnostics
  throw ("RMX3241 CameraX smoke failed; exit=" + $exitLabel)
}

Write-Host ("RMX3241 CameraX bind/rebind smoke passed. Permission dialog handled=" + $permissionHandled)
