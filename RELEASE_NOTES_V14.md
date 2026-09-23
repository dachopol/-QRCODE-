# QuickQR Business v14.0

## Camera / Scanner
- CameraX PreviewView uses COMPATIBLE mode to reduce corrupted/blocky preview rendering in embedded previews and virtual devices.
- Preview and ImageAnalysis use current display rotation.
- Back camera is preferred with front-camera fallback.
- Camera use cases and analyzer executor are cleaned up when leaving Scanner.
- Camera failures are surfaced instead of silently swallowed.
- ZXing uses TRY_HARDER + ALSO_INVERTED.
- Google AI Studio metadata requests camera frame permission.

## Version consistency
- versionCode = 14
- versionName = "14.0"
- UI badge reads BuildConfig.VERSION_NAME.
- applicationId remains com.aistudio.qrgenerator.kmpzqr.

## Cleanup
- Runtime UI languages exposed: Thai and English only.
- Legacy VIP/Ads/free-tier translation strings removed from runtime localization.

## Verification
A fresh Build/Preview or real-device run is required before calling this release runtime-verified.
