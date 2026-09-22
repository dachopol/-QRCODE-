# Device UI Fix Report — QuickQR Business v12

Basis: issues observed on the real-device screenshots supplied by the user.

## Fixed in source
- Generate QR menu uses a fixed 4-column TabRow so PromptPay / Wi‑Fi / Store / Text stay visible on phone screens.
- Thai/English selection updates the main screen copy instead of changing only the flag/code.
- QR color customizer labels and color names switch between Thai and English.
- Business-card preview banner uses minimum height so business/profession text can expand instead of being cropped by a fixed-height container.
- Support/security header can wrap to two lines without pushing the close button off-screen.
- Security re-check, QR preview and scan-result actions use minimum heights instead of clipping long labels.
- Bottom navigation respects the Android navigation-bar inset and keeps all 4 destinations visible.
- Scan results, generated preview titles, history-generated titles and major Toast messages now follow Thai/English selection.
- Language dialog tabs follow the active language.

## Build safety rules retained
- No `import androidx.compose.foundation.layout.weight`.
- `Modifier.weight(...)` remains only where Compose scope supports it.
- Package remains `com.aistudio.qrgenerator.kmpzqr`.
- Version remains `versionCode 12`, `versionName 12.0`.
- The removed personal Gmail address is not present in the project.

## Not claimed as verified here
This environment does not provide the same Android/AI Studio build harness used by Google AI Studio, so the final `compile_applet` / device install should still be run once after import. Do not press AI Fix unless the build log shows a real compiler/runtime error.
