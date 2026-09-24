# QuickQR Business v17 — Google Play Data Safety

Source basis: current GitHub main / v17.0.

## Current technical facts
- Manifest permission: Camera only.
- No `INTERNET` permission.
- No AdMob SDK.
- No Google Play Billing SDK.
- No login/account SDK.
- QR history and business profile use local Room storage.
- Android backup is disabled.
- Camera frames and selected images are processed on-device for QR decoding.
- Generated QR images can be saved locally.
- Sharing is initiated by the user through Android Share Sheet.
- Admin email / bug report is initiated by the user through an external email/share app.

## Suggested Data Safety form answers for current v17

### Does your app collect or share any of the required user data types?
**Suggested: No**, based on current v17 source.

Reason:
The current app does not contain app-controlled networking and no user data transmission to a developer or third-party server was identified.

### Local data accessed by the app
These may exist on-device but are not identified as off-device collection in current v17:
- QR content and QR history
- PromptPay identifier entered by the user
- Business card/profile fields entered by the user
- Wi-Fi SSID/password entered by the user
- Images selected for QR scanning
- Camera frames used for scanning
- Generated QR images
- Language/region/currency preferences

### User-initiated transfers
The app can launch Android system/external apps for:
- Sharing QR images/text
- Sending a bug report
- Contacting admin by email
- Opening scanned links in an external app/browser where applicable

These actions occur only after user action and the destination app/service is selected or opened by the user.

## Deletion
Server-side deletion request: Not applicable because no developer server/account storage exists in current v17.

On-device deletion:
- History can be deleted from the app.
- App data can be cleared by Android settings.
- Uninstalling removes app-private local data.
- Android backup is disabled.

## Re-check triggers
Re-open this declaration before release if any of these are added:
- AdMob or any advertising SDK
- Analytics/crash reporting SDK
- Firebase
- Play Billing
- Internet/network API
- Cloud sync/login
- Crypto price API
- Remote bug-report backend
- Account system
- Location permission
- Contacts/files/background upload

## Important Play Console rule
Data Safety must describe the actual behavior of the versions/variants distributed on Google Play. Re-check the final AAB and any older active track build before submitting.
