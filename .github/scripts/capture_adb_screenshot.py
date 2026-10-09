#!/usr/bin/env python3
import argparse
import pathlib
import subprocess
import sys

PNG_HEADER = b"\x89PNG\r\n\x1a\n"

def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--serial", required=True)
    parser.add_argument("--output", required=True)
    args = parser.parse_args()

    try:
        completed = subprocess.run(
            ["adb", "-s", args.serial, "exec-out", "screencap", "-p"],
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            timeout=60,
            check=False,
        )
    except (OSError, subprocess.TimeoutExpired) as exc:
        print(f"ADB screenshot execution failed: {type(exc).__name__}", file=sys.stderr)
        return 2

    if completed.returncode != 0:
        print("ADB screenshot command returned non-zero exit status", file=sys.stderr)
        return 3
    if not completed.stdout.startswith(PNG_HEADER):
        print(f"ADB screenshot did not return PNG data; bytes={len(completed.stdout)}", file=sys.stderr)
        return 4

    output = pathlib.Path(args.output)
    output.parent.mkdir(parents=True, exist_ok=True)
    output.write_bytes(completed.stdout)
    print(f"screenshot_bytes={len(completed.stdout)}")
    return 0

if __name__ == "__main__":
    raise SystemExit(main())
