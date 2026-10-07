#!/usr/bin/env python3
"""Sanitized, fail-closed Google Play API GET-only release probe for QuickQR.

Expected use: GitHub Actions with an ephemeral service-account JSON file.
Outputs only PASS/FAIL-safe status. It performs no Play edit lifecycle,
release/tester mutation, Production publishing, or credential logging.
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
PUBLISHED = "RELEASE_LIFECYCLE_STATE_PUBLISHED"


class ProbeError(RuntimeError):
    pass


def api_url(*parts: str) -> str:
    return API_ROOT + "/" + "/".join(urllib.parse.quote(p, safe="") for p in parts)


def request_json(url: str, token: str):
    headers = {"Authorization": f"Bearer {token}", "Accept": "application/json"}
    req = urllib.request.Request(url, headers=headers, method="GET")
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
    releases_doc = request_json(
        api_url("applications", package_name, "tracks", "internal", "releases"),
        token,
    ) or {}

    releases = releases_doc.get("releases", []) or []
    published_match = any(
        release.get("releaseLifecycleState") == PUBLISHED
        and any(
            str(artifact.get("versionCode")) == expected_version_code
            for artifact in (release.get("activeArtifacts", []) or [])
        )
        for release in releases
    )
    if not published_match:
        raise ProbeError("expected versionCode not published on internal track")


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
        "PASS: Google Play API authenticated exact-package GET-only read; "
        "Internal Testing has published expected versionCode."
    )
    return 0


if __name__ == "__main__":
    sys.exit(main())
