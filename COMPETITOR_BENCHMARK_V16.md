# QuickQR Business — Competitor benchmark (2026-09-23)

## Public Google Play reference points
- Gamma Play — QR & Barcode Scanner: about 4.8 stars and 500M+ downloads. Strengths: extremely simple auto-scan flow and broad QR/barcode format support.
- TeaCapps — QR & Barcode Scanner: about 4.6 stars and 100M+ downloads. Strengths: common barcode formats and context-specific actions.
- QRbot: about 4.6 stars and 5M+ downloads. Strengths: broad formats and fast actions.
- QR Toolkit: Scanner, Generator: differentiates with on-device preview before opening links, scan-from-image, Wi-Fi/vCard generation, logo/customization, and searchable history.

Sources checked on Google Play on 2026-09-23:
- https://play.google.com/store/apps/details?id=com.gamma.scan
- https://play.google.com/store/apps/details?id=com.teacapps.barcodescanner
- https://play.google.com/store/apps/details?id=net.qrbot
- https://play.google.com/store/apps/details?id=com.djump.qrtoolkit

## Review-derived pain points used for v16
- Accidental/too-fast scan when multiple codes are visible.
- Misleading or intrusive ad interactions can damage trust.
- Users value history, scan-from-image, speed, and clear post-scan actions.

## v16 response
- Center-target camera decoding reduces accidental reads outside the visible frame.
- Same-result callback de-duplication protects history quality.
- Searchable local history improves retrieval.
- Existing gallery scan and preview-before-open flow are retained.
- Thai/English labels are made more consistent.
- Test build keeps fake monetization paths out of runtime.

## Important gap list
These are not claimed as implemented:
- 1D retail barcode support (EAN/UPC/Code 128).
- SVG/vector export.
- History pin/collections.
- Email/SMS/calendar/location QR generators.
- Cross-device history sync.

## Rating note
A Play Store star rating cannot be guaranteed by code changes. The engineering target is to remove common review triggers, improve reliability and clarity, and make QuickQR Business more useful for Thai small-business workflows.
