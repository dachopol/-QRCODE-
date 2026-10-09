$ErrorActionPreference = "Stop"

$gradle = (Get-Command gradle.bat -ErrorAction Stop).Source
$stdout = Join-Path $env:RUNNER_TEMP "quickqr-camera-smoke.stdout.log"
$stderr = Join-Path $env:RUNNER_TEMP "quickqr-camera-smoke.stderr.log"
$localDump = Join-Path $env:RUNNER_TEMP "quickqr-permission.xml"

Remove-Item $stdout, $stderr, $localDump -Force -ErrorAction SilentlyContinue

$arguments = @(
  "--no-daemon",
  ":app:connectedDebugAndroidTest",
  "-Pandroid.testInstrumentationRunnerArguments.class=com.aistudio.qrgenerator.kmpzqr.PhysicalDeviceSmokeInstrumentedTest",
  "--stacktrace"
)

$startArgs = @{
  FilePath = $gradle
  ArgumentList = $arguments
  WorkingDirectory = $PWD.Path
  RedirectStandardOutput = $stdout
  RedirectStandardError = $stderr
  PassThru = $true
  WindowStyle = "Hidden"
}
$process = Start-Process @startArgs

$deadline = [DateTime]::UtcNow.AddMinutes(4)
$permissionHandled = $false

while (-not $process.HasExited -and [DateTime]::UtcNow -lt $deadline) {
  Start-Sleep -Seconds 2

  try {
    & adb shell uiautomator dump /sdcard/quickqr_permission.xml 2>$null | Out-Null
    & adb pull /sdcard/quickqr_permission.xml $localDump 2>$null | Out-Null

    if (Test-Path -LiteralPath $localDump -PathType Leaf) {
      [xml]$xml = Get-Content -Raw -LiteralPath $localDump
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
          & adb shell input tap $x $y | Out-Null
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
  throw "RMX3241 CameraX smoke exceeded 4-minute bound"
}

$process.WaitForExit()

if (Test-Path -LiteralPath $stdout) {
  Get-Content -LiteralPath $stdout | Out-Host
}
if (Test-Path -LiteralPath $stderr) {
  Get-Content -LiteralPath $stderr | Out-Host
}

if ($process.ExitCode -ne 0) {
  throw ("RMX3241 CameraX smoke failed with exit code " + $process.ExitCode)
}

Write-Host ("RMX3241 CameraX bind/rebind smoke passed. Permission dialog handled=" + $permissionHandled)
