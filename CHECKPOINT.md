# QuickQR continuation checkpoint

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
