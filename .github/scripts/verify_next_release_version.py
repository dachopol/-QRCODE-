#!/usr/bin/env python3
"""Fail-closed versionCode boundary check for QuickQR next-release PRs."""

from __future__ import annotations

import argparse
import re
import subprocess
import sys
from pathlib import Path

VERSION_CODE_RE = re.compile(r"^\s*versionCode\s*=\s*(\d+)\s*$", re.MULTILINE)


class VersionBoundaryError(RuntimeError):
    pass


def parse_version_code(text: str) -> int:
    matches = VERSION_CODE_RE.findall(text)
    if len(matches) != 1:
        raise VersionBoundaryError(
            f"Expected exactly one versionCode assignment, found {len(matches)}"
        )
    return int(matches[0])


def read_git_file(ref: str, path: str) -> str:
    try:
        return subprocess.check_output(
            ["git", "show", f"{ref}:{path}"],
            text=True,
            stderr=subprocess.STDOUT,
        )
    except subprocess.CalledProcessError as exc:
        raise VersionBoundaryError(
            f"Unable to read {path} from base ref {ref}"
        ) from exc


def verify(base_text: str, head_text: str) -> tuple[int, int]:
    base = parse_version_code(base_text)
    head = parse_version_code(head_text)
    if head <= base:
        raise VersionBoundaryError(
            f"next-release versionCode must be greater than main: base={base}, head={head}"
        )
    return base, head


def self_test() -> None:
    assert parse_version_code("versionCode = 19\n") == 19
    assert parse_version_code("  versionCode = 20\n") == 20

    try:
        parse_version_code("versionName = \"18.0\"\n")
    except VersionBoundaryError:
        pass
    else:
        raise AssertionError("missing versionCode must fail")

    try:
        verify("versionCode = 19\n", "versionCode = 19\n")
    except VersionBoundaryError:
        pass
    else:
        raise AssertionError("same versionCode must fail")

    try:
        verify("versionCode = 19\n", "versionCode = 18\n")
    except VersionBoundaryError:
        pass
    else:
        raise AssertionError("lower versionCode must fail")

    assert verify("versionCode = 19\n", "versionCode = 20\n") == (19, 20)


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--self-test", action="store_true")
    parser.add_argument("--base-ref")
    parser.add_argument("--head-file", default="app/build.gradle.kts")
    args = parser.parse_args()

    try:
        if args.self_test:
            self_test()
            print("PASS: next-release version guard self-test")
            return 0

        if not args.base_ref:
            raise VersionBoundaryError("--base-ref is required")

        base_text = read_git_file(args.base_ref, args.head_file)
        head_text = Path(args.head_file).read_text(encoding="utf-8")
        base, head = verify(base_text, head_text)
        print(f"PASS: next-release versionCode boundary base={base} head={head}")
        return 0
    except (VersionBoundaryError, OSError) as exc:
        print(f"FAIL: {exc}")
        return 2


if __name__ == "__main__":
    sys.exit(main())
