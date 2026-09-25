# QuickQR continuation checkpoint

## Latest verified outcome (supersedes prior blocked-build notes)
- User approved main upload. Code commit: 0c61307bb175f4f2e91583cf7a4c9a2e7ae145f6.
- Build/test PASS on GitHub: assembleDebug + testDebugUnitTest; APK upload PASS.
- Evidence: https://github.com/dachopol/-QRCODE-/actions/runs/36067250936
- Local git push lacked credentials; upload used the configured GitHub connector.
- Local main was aligned with the remote; local-prepared-quickqr preserves the
  original local commit with identical file contents.
- Remaining: real-device TH/EN, layout, QR actions, camera and competitor measures.
- Follow the remaining device checks below; do not repeat already-passed CI
  unless code changes or a concrete new risk requires it.

## Source
- Repository: dachopol/-QRCODE-, main
- Base commit: 0d158c5493b3aebc490a004a7c38fe8ad20ad470
- Version: 17.0 (17); package unchanged.
- No prior CHECKPOINT.md or task_state.json existed in this checkout.

## Current task: canva-generator-ui
Reviewed Canva page 1 thumbnail from design DAHWJiLYKhA:
https://canva.link/sog58xmb2rklkq2
Applied its form-first hierarchy to GeneratorScreen. Main forms and Generate
actions now precede a collapsible appearance card. All existing color/logo controls
remain available, with expansion state saved. Dark card labels use onSurface;
white-field placeholders use a darker slate. Added password visibility labels,
localized Open, room for hidden-network text, larger platform targets and wrapping
for store/text Generate labels. No new dependency, package or version change.
Static diff check passes. Build retry still exits 127: gradle command not found.
Runtime appearance, small-screen layout and TH/EN switching remain unverified.

## Prior pending task: generator-language-mixing
User screenshots show EN with Thai Wi-Fi SSID/password placeholders, Website
platform label, and LINE ID placeholder. Fixed these in GeneratorScreen.kt using
the existing reactive localizedText helper; covered all store platform placeholder
variants. Static diff check passes; runtime/build remain blocked as described below.

## Prior pending task: wifi-escaped-payload-regression
The generator escapes Wi-Fi fields, but the scanner used split(";") and did not
unescape them. This truncated SSIDs/passwords and could interpret part of an SSID
as another field. Replaced splitting with escape-aware field parsing.

Added three JUnit regression tests: independent escaped payload, generator/parser
round trip with Thai and special characters, and a literal trailing backslash
with an open network. Existing plain Wi-Fi test remains.

## Evidence and limits
- Base Android CI passed compile, unit tests and APK upload:
  https://github.com/dachopol/-QRCODE-/actions/runs/35960276382
- git diff --check passes for the local patch.
- New tests and Android build are NOT RUN: gradle command exited 127 (not found).
- No adb or Gradle on PATH; no Android SDK at checked conventional locations.
- Gradle distribution request timed out, curl exit 28, after redirect to GitHub.
- The device test currently only checks application ID, not scan performance.
- Local changes have NOT been pushed; base CI does not validate this patch.
- No device/camera performance or competitor quality score is established.

## Resume
1. Review the patch and run on an Android build runner with JDK 17, Gradle 9.3.1,
   Android SDK 36: gradle --no-daemon :app:assembleDebug :app:testDebugUnitTest.
2. Inspect failures and fix root cause; do not report the patch as passing yet.
3. Test generated Wi-Fi QR with special characters using gallery and real camera;
   confirm displayed/copied SSID and password exactly match inputs.
4. Measure competitors and QuickQR on the same phone, QR fixtures, lighting and
   distances. Record decoding success and elapsed time; leave scores unknown
   until measurements exist. UI/privacy assessment requires separate evidence.

DoD remains blocked on build/test of the patch and real-device runtime validation.
# Full-app Canva style continuation — 2026-09-25

- Applied semantic light/dark surfaces to navigation, history, card forms, QR customization, preview, language/region and support screens.
- Stacked business contact fields, enlarged color/reset/close controls, and made history filters scroll at large font sizes.
- Localized scan-result type badges for TH/EN. Preserved QR bitmap palettes, card preview colors and existing actions.
- Canva generation remains blocked by quota_exceeded; this continuation updates Android source using the existing design direction.
- Current patch: diff check passed; CI build/unit tests pending. Previous CI result below applies only to 0c61307.
- Device validation (small-screen TH/EN, light/dark, font scaling, navigation, scan/save/share) is still required.
