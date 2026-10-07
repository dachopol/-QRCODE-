#!/usr/bin/env python3
"""Sanitized, fail-closed Google Play API read probe for QuickQR.

Expected use: GitHub Actions with an ephemeral service-account JSON file.
Outputs only PASS/FAIL-safe status. It never commits a Play edit and never
prints credentials, tester identities, release notes, or full API payloads.
"""

from __future__ import annotations

import argparse
import json
import sys
import urllib.error
import urllib.parse
import urllib.request

from google.auth.transport.requests import Request
from google.oauth2 import service_account

API_ROOT = "https://androidpublisher.googleapis.com/androidpublisher/v3"
SCOPE = "https://www.googleapis.com/auth/androidpublisher"


class ProbeError(RuntimeError):
    pass


def api_url(*parts: str) -> str:
    return API_ROOT + "/" + "/".join(urllib.parse.quote(p, safe="") for p in parts)


def request_json(method: str, url: str, token: str, payload=None):
    data = None if payload is None else json.dumps(payload).encode("utf-8")
    headers = {"Authorization": f"Bearer {token}", "Accept": "application/json"}
    if data is not None:
        headers["Content-Type"] = "application/json"
    req = urllib.request.Request(url, data=data, headers=headers, method=method)
    try:
        with urllib.request.urlopen(req, timeout=30) as response:
            raw = response.read()
            return None if not raw else json.loads(raw.decode("utf-8"))
    except urllib.error.HTTPError as exc:
        # Never echo provider response bodies because they may contain account detail.
        raise ProbeError(f"Google Play API HTTP {exc.code}") from exc
    except urllib.error.URLError as exc:
        raise ProbeError("Google Play API network error") from exc


def access_token(service_account_file: str) -> str:
    credentials = service_account.Credentials.from_service_account_file(
        service_account_file,
        scopes=[SCOPE],
    )
    credentials.refresh(Request())
    if not credentials.token:
        raise ProbeError("OAuth token refresh returned no token")
    return credentials.token


def run_probe(package_name: str, expected_version_code: str, service_account_file: str) -> None:
    token = access_token(service_account_file)
    edit_id = None
    cleanup_ok = False
    try:
        edit = request_json("POST", api_url("applications", package_name, "edits"), token, {}) or {}
        edit_id = str(edit.get("id") or "")
        if not edit_id:
            raise ProbeError("temporary edit id missing")

        internal = request_json(
            "GET",
            api_url("applications", package_name, "edits", edit_id, "tracks", "internal"),
            token,
        ) or {}

        releases = internal.get("releases", []) or []
        present = any(
            expected_version_code in [str(v) for v in (release.get("versionCodes", []) or [])]
            for release in releases
        )
        if not present:
            raise ProbeError("expected versionCode not present on internal track")
    finally:
        if edit_id:
            try:
                request_json(
                    "DELETE",
                    api_url("applications", package_name, "edits", edit_id),
                    token,
                )
                cleanup_ok = True
            except Exception as exc:
                raise ProbeError("temporary edit cleanup failed") from exc

    if not cleanup_ok:
        raise ProbeError("temporary edit cleanup not confirmed")


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--package", required=True)
    parser.add_argument("--expected-version-code", required=True)
    parser.add_argument("--service-account-file", required=True)
    args = parser.parse_args()

    try:
        run_probe(args.package, args.expected_version_code, args.service_account_file)
    except Exception as exc:
        print(f"FAIL: {type(exc).__name__}: {exc}")
        return 2

    print(
        "PASS: Google Play API authenticated exact-package read; "
        "Internal Testing contains expected versionCode; temporary edit deleted."
    )
    return 0


if __name__ == "__main__":
    sys.exit(main())
