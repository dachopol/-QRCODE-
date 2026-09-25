param(
    [string]$GradleCommand = "gradle"
)

$ErrorActionPreference = "Stop"

$required = @("KEYSTORE_PATH", "STORE_PASSWORD", "KEY_PASSWORD")
$missing = @()
foreach ($name in $required) {
    $value = [Environment]::GetEnvironmentVariable($name)
    if ([string]::IsNullOrWhiteSpace($value)) {
        $missing += $name
    }
}

if ($missing.Count -gt 0) {
    throw "Missing release signing environment variable(s): $($missing -join ', ')"
}

$keystorePath = [Environment]::GetEnvironmentVariable("KEYSTORE_PATH")
if (-not (Test-Path -LiteralPath $keystorePath)) {
    throw "KEYSTORE_PATH does not point to an existing file."
}

$keyAlias = [Environment]::GetEnvironmentVariable("KEY_ALIAS")
if ([string]::IsNullOrWhiteSpace($keyAlias)) {
    $keyAlias = "upload"
}

Write-Host "Signing preflight: PASS"
Write-Host "Keystore file: present"
Write-Host "Key alias: $keyAlias"
Write-Host "Passwords: present (values hidden)"

& $GradleCommand --no-daemon :app:bundleRelease
if ($LASTEXITCODE -ne 0) {
    throw "Gradle release bundle failed."
}

$aab = Join-Path $PSScriptRoot "..\app\build\outputs\bundle\release\app-release.aab"
$aab = [System.IO.Path]::GetFullPath($aab)
if (-not (Test-Path -LiteralPath $aab)) {
    throw "Release AAB was not created."
}

$jar = Get-Command jarsigner -ErrorAction SilentlyContinue
if ($null -eq $jar) {
    throw "jarsigner was not found in PATH; install/use JDK 17 and retry."
}

& $jar.Source -verify -strict -certs $aab
if ($LASTEXITCODE -ne 0) {
    throw "Release AAB signature verification failed."
}

$hash = (Get-FileHash -LiteralPath $aab -Algorithm SHA256).Hash
Write-Host "SIGNED_RELEASE_AAB=$aab"
Write-Host "SHA256=$hash"
