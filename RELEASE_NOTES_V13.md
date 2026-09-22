# QuickQR Business v13 — Release notes

## Version
- applicationId: `com.aistudio.qrgenerator.kmpzqr`
- versionCode: `13`
- versionName: `13.0`

## Language consistency
- TH uses Thai screen-wide.
- EN uses English screen-wide.
- Languages that are not yet complete fall back screen-wide to English instead of mixing languages.
- Version badge reads from `BuildConfig.VERSION_NAME`.

## Card-only cleanup
- Removed header subtitle.
- Removed duplicate main-screen support/wallet actions.
- Removed technical QR metadata from the main customizer UI.
- Removed visible QR size/padding/HEX implementation notes.
- Removed nonessential sub-text under Generator and Business Card headings.
- Preserved core QR functions.

## Region / currency
- Region is the source of truth for currency selection.
- Device region is used to choose the initial currency when no saved region exists.
- Region cards show one region with its corresponding currency.
- Header separates language from region/currency.
- PromptPay amount remains THB only.
- Removed live foreign-currency conversion from the PromptPay flow.
- Hardcoded FX rates are not shown as current rates in the main UI.

## Status
- Source updated on GitHub main.
- Fresh Google AI Studio build/device verification is still required before Play Console release.


## Step 4 — Visual system
- Added shared visual tokens for 28dp cards, pill actions, 20dp sections, subtle glass accent, and 40dp blur token.
- Applied ~5% glass accent to the app shell and key screens without obscuring content.
- Applied 28dp primary card radius to Generator, Business Card, Scanner, History, QR Preview, and Language/Region surfaces.
- Applied pill shapes to primary actions and selected navigation/tab treatments.
- Added a subtle 40dp blurred glass accent to the QR customizer only, keeping blur away from text and controls.
- Set base letter spacing to 0sp for Thai readability.
- Preserved existing routes, QR functions, PromptPay logic, scanner, history, localization, and region/currency rules.
