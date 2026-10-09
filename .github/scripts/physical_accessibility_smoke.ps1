$ErrorActionPreference = "Stop"

if ([string]::IsNullOrWhiteSpace($env:ADB_SERIAL)) {
  throw "ADB_SERIAL is required for RMX3241 physical QA"
}
if ([string]::IsNullOrWhiteSpace($env:TEST_INSTRUMENTATION)) {
  throw "TEST_INSTRUMENTATION is required for RMX3241 physical QA"
}

New-Item -ItemType Directory -Force -Path "physical-evidence" | Out-Null

$originalScale = (& adb -s $env:ADB_SERIAL shell settings get system font_scale).Trim()
if ([string]::IsNullOrWhiteSpace($originalScale) -or $originalScale -eq "null") {
  $originalScale = "1.0"
}

$failure = $null
try {
  & adb -s $env:ADB_SERIAL shell settings put system font_scale 1.3
  if ($LASTEXITCODE -ne 0) { throw "Could not set physical font_scale=1.3" }
  Start-Sleep -Seconds 2
  & adb -s $env:ADB_SERIAL shell am force-stop $env:APP_ID | Out-Null

  $previousErrorPreference = $ErrorActionPreference
  $ErrorActionPreference = "Continue"
  $result = (& adb -s $env:ADB_SERIAL shell am instrument -w -r -e class com.aistudio.qrgenerator.kmpzqr.PhysicalAccessibilityInstrumentedTest -e largeFont true $env:TEST_INSTRUMENTATION 2>&1) -join "\n"
  $instrumentExit = $LASTEXITCODE
  $ErrorActionPreference = $previousErrorPreference

  $result | Out-Host
  $reportedSuccess =
    $result -match "(?m)^INSTRUMENTATION_STATUS_CODE:\s*0\s*$" -and
    $result -match "(?m)^INSTRUMENTATION_CODE:\s*-1\s*$"
  $reportedFailure =
    $result -match "FAILURES!!!|INSTRUMENTATION_FAILED|Process crashed|INSTRUMENTATION_STATUS:\s*stack="

  if ($instrumentExit -ne 0 -or -not $reportedSuccess -or $reportedFailure) {
    throw ("RMX3241 large-font accessibility instrumentation failed; exit=" + $instrumentExit)
  }

  & adb -s $env:ADB_SERIAL shell am force-stop $env:APP_ID | Out-Null
  $launch = (& adb -s $env:ADB_SERIAL shell am start -W -n "$env:APP_ID/.MainActivity" 2>&1) -join "\n"
  if ($LASTEXITCODE -ne 0 -or $launch -notmatch "Status:\s+ok") {
    throw "Could not relaunch app for large-font evidence"
  }
  Start-Sleep -Seconds 3

  & adb -s $env:ADB_SERIAL shell screencap -p /sdcard/quickqr_large_font.png
  if ($LASTEXITCODE -ne 0) { throw "Could not capture large-font screenshot" }

  $previousErrorPreference = $ErrorActionPreference
  $ErrorActionPreference = "Continue"
  $pull = (& adb -s $env:ADB_SERIAL pull /sdcard/quickqr_large_font.png "physical-evidence\quickqr_large_font.png" 2>&1) -join "\n"
  $pullExit = $LASTEXITCODE
  $ErrorActionPreference = $previousErrorPreference
  $pull | Out-Host
  if ($pullExit -ne 0) { throw "Could not pull large-font screenshot" }

  $largeFontEvidence = Join-Path $PWD "physical-evidence\quickqr_large_font.png"
  if (-not (Test-Path -LiteralPath $largeFontEvidence -PathType Leaf)) {
    throw "Large-font screenshot evidence is missing"
  }
  if ((Get-Item -LiteralPath $largeFontEvidence).Length -le 0) {
    throw "Large-font screenshot evidence is empty"
  }
} catch {
  $failure = $_.Exception.Message
} finally {
  & adb -s $env:ADB_SERIAL shell settings put system font_scale $originalScale | Out-Null
  Start-Sleep -Seconds 1
  & adb -s $env:ADB_SERIAL shell am force-stop $env:APP_ID | Out-Null
  & adb -s $env:ADB_SERIAL shell am start -W -n "$env:APP_ID/.MainActivity" | Out-Null
}

if ($failure) {
  throw $failure
}

Write-Host ("RMX3241 large-font accessibility smoke passed; restored font_scale=" + $originalScale)
