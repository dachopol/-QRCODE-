#!/usr/bin/env python3
import argparse
import pathlib
import subprocess
import sys

PNG_HEADER = b"\x89PNG\r\n\x1a\n"
REMOTE_FALLBACK = "/data/local/tmp/quickqr-physical-screenshot.png"


def run(cmd, timeout=60):
    return subprocess.run(
        cmd,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        timeout=timeout,
        check=False,
    )


def valid_png(data: bytes) -> bool:
    return data.startswith(PNG_HEADER) and len(data) > len(PNG_HEADER)


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--serial", required=True)
    parser.add_argument("--output", required=True)
    args = parser.parse_args()

    output = pathlib.Path(args.output)
    output.parent.mkdir(parents=True, exist_ok=True)

    try:
        direct = run(["adb", "-s", args.serial, "exec-out", "screencap", "-p"])
    except (OSError, subprocess.TimeoutExpired) as exc:
        print(f"ADB direct screenshot execution failed: {type(exc).__name__}", file=sys.stderr)
        direct = None

    if direct is not None and direct.returncode == 0 and valid_png(direct.stdout):
        output.write_bytes(direct.stdout)
        print(f"screenshot_mode=exec-out bytes={len(direct.stdout)}")
        return 0

    print(
        "ADB exec-out screenshot unavailable or empty; using shell screencap + pull fallback",
        file=sys.stderr,
    )

    try:
        capture = run([
            "adb", "-s", args.serial, "shell", "screencap", "-p", REMOTE_FALLBACK
        ])
        if capture.returncode != 0:
            print("ADB fallback screencap command failed", file=sys.stderr)
            return 3

        pull = run([
            "adb", "-s", args.serial, "pull", REMOTE_FALLBACK, str(output)
        ])
        if pull.returncode != 0:
            print("ADB fallback pull command failed", file=sys.stderr)
            return 4
    except (OSError, subprocess.TimeoutExpired) as exc:
        print(f"ADB fallback screenshot execution failed: {type(exc).__name__}", file=sys.stderr)
        return 5
    finally:
        try:
            run(["adb", "-s", args.serial, "shell", "rm", "-f", REMOTE_FALLBACK], timeout=15)
        except (OSError, subprocess.TimeoutExpired):
            pass

    try:
        data = output.read_bytes()
    except OSError as exc:
        print(f"Pulled screenshot could not be read: {type(exc).__name__}", file=sys.stderr)
        return 6

    if not valid_png(data):
        print(f"Pulled screenshot is not valid PNG data; bytes={len(data)}", file=sys.stderr)
        return 7

    print(f"screenshot_mode=shell-pull bytes={len(data)}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
