#!/usr/bin/env python3
"""QuickQR read-only A1 triage and A5 offline evidence-integrity shadow verifier.

Neither command authorizes a release or signs an independent verdict. Synthetic
self-tests prove only the verifier's behavior, never physical-device runtime.
"""
import argparse
import hashlib
import json
import re
import sys
import tempfile
from pathlib import Path

SHA40 = re.compile(r"^[0-9a-f]{40}$")
SHA256 = re.compile(r"^[0-9a-f]{64}$")
REPO = "dachopol/-QRCODE-"
PACKAGE = "com.aistudio.qrgenerator.kmpzqr"
REQUIRED = frozenset({
    "build", "hosted-instrumentation", "android16", "physical-camera-open",
    "physical-camera-close", "physical-qr-decode", "physical-location",
    "device-auth", "device-ui", "accessibility", "logcat", "device-cleanup"
})
PHYSICAL = frozenset(k for k in REQUIRED if k.startswith("physical-")) | {
    "device-auth", "device-ui", "accessibility", "logcat", "device-cleanup"
}
PATTERNS = {
    "COMPOSE_HIERARCHY_MISSING": r"No compose hierarchies found",
    "PROCESS_CRASH_MARKER": r"FATAL EXCEPTION|Process crashed",
    "INSTRUMENTATION_FAILED": r"INSTRUMENTATION_FAILED|FAILURES!!!|Physical camera/location smoke failed",
    "DEVICE_LOCK_UNVERIFIED": r"device_locked=precheck_unverified_oem",
    "LARGE_FONT_BLOCKED": r"large_font_core_controls=blocked_",
    "ADB_TIMEOUT": r"ADB command timed out",
}
MAX_LOG_BYTES = 4 * 1024 * 1024


def digest(path):
    h = hashlib.sha256()
    with path.open("rb") as source:
        while True:
            block = source.read(65536)
            if not block:
                break
            h.update(block)
    return h.hexdigest()


def output(**fields):
    print(json.dumps(fields, ensure_ascii=False, sort_keys=True, separators=(",", ":")))


def triage(logfiles, source_sha):
    if not SHA40.fullmatch(source_sha):
        return {"agent": "A1", "decision": "BLOCKED", "reason_codes": ["INVALID_SOURCE_SHA"]}
    counts = {name: 0 for name in PATTERNS}
    sources = []
    for name in logfiles:
        path = Path(name)
        if not path.is_file() or path.is_symlink() or path.stat().st_size > MAX_LOG_BYTES:
            return {"agent": "A1", "decision": "BLOCKED", "reason_codes": ["INVALID_LOG_INPUT"]}
        blob = path.read_bytes()
        text = blob.decode("utf-8", errors="replace")
        for code, pattern in PATTERNS.items():
            counts[code] += len(re.findall(pattern, text, flags=re.IGNORECASE))
        sources.append({"sha256": hashlib.sha256(blob).hexdigest(), "bytes": len(blob)})
    codes = [{"code": k, "count": v} for k, v in sorted(counts.items()) if v]
    return {
        "agent": "A1", "decision": "INCONCLUSIVE", "source_sha": source_sha,
        "suspected_failure": bool(codes), "observations": codes,
        "root_cause": "UNCONFIRMED", "inputs": sources,
        "note": "Read-only patterns are not an independent runtime verdict"
    }


def verify(bundle_dir, expected_sha):
    base = Path(bundle_dir).resolve(strict=True)
    if not SHA40.fullmatch(expected_sha):
        return {"agent": "A5", "decision": "BLOCKED", "reason_codes": ["INVALID_SOURCE_PIN"]}
    manifest_path = base / "manifest.json"
    if not manifest_path.is_file() or manifest_path.is_symlink():
        return {"agent": "A5", "decision": "BLOCKED", "reason_codes": ["MANIFEST_MISSING"]}
    try:
        m = json.loads(manifest_path.read_text(encoding="utf-8"))
    except (ValueError, UnicodeError, OSError):
        return {"agent": "A5", "decision": "BLOCKED", "reason_codes": ["INVALID_MANIFEST_JSON"]}
    if not isinstance(m, dict) or m.get("schema") != "quickqr-evidence-v1":
        return {"agent": "A5", "decision": "BLOCKED", "reason_codes": ["INVALID_SCHEMA"]}
    ident = m.get("identity")
    if not isinstance(ident, dict):
        return {"agent": "A5", "decision": "BLOCKED", "reason_codes": ["IDENTITY_MISSING"]}
    sha = ident.get("source_sha")
    artifact = ident.get("artifact_sha256")
    run_id = ident.get("run_id")
    if not (
        ident.get("repository") == REPO and ident.get("package") == PACKAGE
        and isinstance(sha, str) and SHA40.fullmatch(sha)
        and isinstance(artifact, str) and SHA256.fullmatch(artifact)
        and type(run_id) is int and run_id > 0
        and type(ident.get("version_code")) is int and ident["version_code"] > 0
    ):
        return {"agent": "A5", "decision": "BLOCKED", "reason_codes": ["IDENTITY_INVALID"]}
    if sha != expected_sha:
        return {"agent": "A5", "decision": "QUARANTINED", "reason_codes": ["SOURCE_PIN_MISMATCH"]}
    receipts = m.get("receipts")
    if not isinstance(receipts, list):
        return {"agent": "A5", "decision": "BLOCKED", "reason_codes": ["RECEIPTS_MISSING"]}
    kinds, ids, paths, issues = set(), set(), set(), set()
    for rec in receipts:
        if not isinstance(rec, dict):
            issues.add("INVALID_RECEIPT")
            continue
        kind, rid, relative = rec.get("kind"), rec.get("id"), rec.get("path")
        if not isinstance(kind, str) or not isinstance(rid, str) or not rid:
            issues.add("INVALID_RECEIPT")
            continue
        if rid in ids:
            issues.add("DUPLICATE_RECEIPT_ID")
        ids.add(rid)
        kinds.add(kind)
        if (rec.get("source_sha") != sha or rec.get("run_id") != run_id
                or rec.get("artifact_sha256") != artifact):
            issues.add("RECEIPT_IDENTITY_MISMATCH")
        if kind in PHYSICAL and rec.get("device_model") != "RMX3241":
            issues.add("PHYSICAL_DEVICE_MISMATCH")
        if not isinstance(relative, str) or not relative or "\\" in relative:
            issues.add("INVALID_RECEIPT_PATH")
            continue
        if relative in paths:
            issues.add("DUPLICATE_EVIDENCE_PATH")
        paths.add(relative)
        file = (base / relative).resolve()
        try:
            file.relative_to(base)
        except ValueError:
            issues.add("PATH_ESCAPE")
            continue
        if not file.is_file() or file.is_symlink():
            issues.add("RECEIPT_FILE_MISSING")
            continue
        claimed = rec.get("sha256")
        if not isinstance(claimed, str) or not SHA256.fullmatch(claimed) or digest(file) != claimed:
            issues.add("EVIDENCE_HASH_MISMATCH")
    if issues:
        return {"agent": "A5", "decision": "QUARANTINED", "reason_codes": sorted(issues),
                "source_sha": sha}
    missing = sorted(REQUIRED - kinds)
    if missing:
        return {"agent": "A5", "decision": "BLOCKED",
                "reason_codes": ["REQUIRED_EVIDENCE_MISSING"], "missing_kinds": missing,
                "source_sha": sha}
    # File/hash/lineage consistency does not establish who ran the tests.
    # Even a complete synthetic manifest MUST NOT produce a runtime PASS.
    return {"agent": "A5", "decision": "INCONCLUSIVE", "source_sha": sha,
            "integrity": "CONSISTENT_UNAUTHENTICATED",
            "reason_codes": ["INDEPENDENT_ATTESTATION_UNVERIFIED"],
            "verified_receipts": len(receipts)}


def self_test():
    """Synthetic fixtures only. No live QA or release claims."""
    src = "a" * 40
    checksum = "b" * 64
    with tempfile.TemporaryDirectory() as temp:
        base = Path(temp)
        manifest = {
            "schema": "quickqr-evidence-v1",
            "identity": {"repository": REPO, "package": PACKAGE, "source_sha": src,
                         "artifact_sha256": checksum, "run_id": 7, "version_code": 20},
            "receipts": [],
        }
        def save():
            (base / "manifest.json").write_text(json.dumps(manifest), encoding="utf-8")
        save()
        assert verify(base, src)["decision"] == "BLOCKED"  # no receipts
        for index, kind in enumerate(sorted(REQUIRED)):
            file = base / ("fixture-" + str(index) + ".txt")
            file.write_text("SYNTHETIC_ONLY_" + kind, encoding="utf-8")
            rec = {"id": "r" + str(index), "kind": kind, "path": file.name,
                   "sha256": digest(file), "source_sha": src, "run_id": 7,
                   "artifact_sha256": checksum}
            if kind in PHYSICAL:
                rec["device_model"] = "RMX3241"
            manifest["receipts"].append(rec)
        save()
        assert verify(base, src)["decision"] == "INCONCLUSIVE"  # never PASS
        assert verify(base, "c" * 40)["decision"] == "QUARANTINED"
        fixture = base / manifest["receipts"][0]["path"]
        fixture.write_text("TAMPER", encoding="utf-8")
        assert "EVIDENCE_HASH_MISMATCH" in verify(base, src)["reason_codes"]
        manifest["receipts"][0]["sha256"] = digest(fixture)
        manifest["receipts"][0]["path"] = "../outside"
        save()
        assert "PATH_ESCAPE" in verify(base, src)["reason_codes"] or "RECEIPT_FILE_MISSING" in verify(base, src)["reason_codes"]
        manifest["receipts"][0]["path"] = fixture.name
        save()
        log = base / "failure.log"
        log.write_text("No compose hierarchies found\nFATAL EXCEPTION", encoding="utf-8")
        result = triage([str(log)], src)
        assert result["decision"] == "INCONCLUSIVE"
        assert result["root_cause"] == "UNCONFIRMED"
        assert len(result["observations"]) == 2
    output(agent="SELF_TEST", fixture_origin="SYNTHETIC_ONLY", tests=7,
           decision="SELF_TEST_OK_NOT_RUNTIME_PASS")


def main():
    ap = argparse.ArgumentParser(description="Offline QuickQR read-only shadow agents")
    subs = ap.add_subparsers(dest="mode", required=True)
    tri = subs.add_parser("triage")
    tri.add_argument("--source-sha", required=True)
    tri.add_argument("--log", action="append", required=True)
    ver = subs.add_parser("verify")
    ver.add_argument("--expected-source-sha", required=True)
    ver.add_argument("--bundle", required=True)
    subs.add_parser("self-test")
    a = ap.parse_args()
    if a.mode == "self-test":
        self_test()
        return 0
    try:
        result = triage(a.log, a.source_sha) if a.mode == "triage" else verify(
            a.bundle, a.expected_source_sha)
    except (OSError, ValueError) as exc:
        result = {"agent": "A1" if a.mode == "triage" else "A5",
                  "decision": "BLOCKED", "reason_codes": ["INPUT_READ_ERROR"],
                  "error_type": type(exc).__name__}
    output(**result)
    return 0 if result["decision"] == "INCONCLUSIVE" else 2


if __name__ == "__main__":
    sys.exit(main())
