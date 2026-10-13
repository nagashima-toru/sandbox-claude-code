#!/usr/bin/env python3
"""Fail when a tracked Markdown file has a relative link to a missing path.

Usage: python3 scripts/check-doc-links.py  (run from anywhere inside the repo)
"""
import os
import re
import subprocess
import sys

LINK = re.compile(r"\]\(([^)\s]+)\)")
INLINE_CODE = re.compile(r"`[^`]*`")
EXTERNAL = re.compile(r"^(https?:|mailto:|#)")


def main() -> int:
    root = subprocess.run(
        ["git", "rev-parse", "--show-toplevel"], capture_output=True, text=True, check=True
    ).stdout.strip()
    files = subprocess.run(
        ["git", "ls-files", "*.md"], cwd=root, capture_output=True, text=True, check=True
    ).stdout.split()

    broken = []
    for rel in files:
        in_fence = False
        with open(os.path.join(root, rel), encoding="utf-8") as f:
            for lineno, line in enumerate(f, 1):
                if line.lstrip().startswith(("```", "~~~")):
                    in_fence = not in_fence
                    continue
                if in_fence:
                    continue
                for target in LINK.findall(INLINE_CODE.sub("", line)):
                    path = target.split("#")[0]
                    if EXTERNAL.match(target) or not path:
                        continue
                    base = root if path.startswith("/") else os.path.join(root, os.path.dirname(rel))
                    if not os.path.exists(os.path.normpath(os.path.join(base, path.lstrip("/")))):
                        broken.append(f"{rel}:{lineno} -> {target}")

    for b in broken:
        print(b)
    print(f"Doc links: {len(files)} file(s), {len(broken)} broken")
    return 1 if broken else 0


if __name__ == "__main__":
    sys.exit(main())
