# QuickQR Business v15.0

## User-facing changes
- Removed the visible Security status tab/page from the Support bottom sheet.
- Anti-Root checks remain active in the app lifecycle; security protection was not removed.
- Admin contact now uses a hidden email target behind the "Email admin" button; the raw address is not displayed in the UI.
- Support sheet now focuses on Report bug, Contact admin, and FAQ.

## Scanner / layout
- Scanner frame adapts to remaining vertical space for small and landscape screens.
- Flash action is shown only when the active camera reports a flash unit.
- Gallery action remains responsive on narrow screens.

## Reliability
- GitHub Actions validates JDK 17, Android SDK 36, Gradle 9.3.1, assembleDebug, and unit tests.
- Scanner parser regression tests cover PromptPay, Wi-Fi, URL normalization, and vCard.
- Debug APK is uploaded as a CI artifact after successful build/test.

## Version
- applicationId: `com.aistudio.qrgenerator.kmpzqr`
- versionCode: `15`
- versionName: `15.0`
- UI version badge continues to use `BuildConfig.VERSION_NAME`.

## Release caution
- The configured admin target is a GitHub `users.noreply.github.com` address per owner instruction. It can be used to open the email composer, but a separate receivable support address is still required for Play Console support contact.
