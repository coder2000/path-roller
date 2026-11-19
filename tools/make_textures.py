"""Generates the roller item texture and the mod icon as PNGs (no third-party dependencies).

Run from the repository root:  python tools/make_textures.py
"""
import struct
import zlib
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
ASSETS = ROOT / "src/main/resources/assets/trailblazer"

PALETTE = {
    ".": (0, 0, 0, 0),
    "g": (58, 58, 58, 255),      # rubber grip
    "h": (150, 100, 52, 255),    # handle, light
    "H": (99, 64, 30, 255),      # handle, shadow
    "k": (78, 78, 84, 255),      # yoke / drum rim
    "d": (200, 200, 206, 255),   # drum highlight
    "D": (150, 150, 158, 255),   # drum body
    "e": (60, 60, 66, 255),      # drum end caps
}

ROLLER = [
    ".............gg.",
    "............ggg.",
    "...........hgg..",
    "..........hH....",
    ".........hH.....",
    "........hH......",
    ".......hH.......",
    "......hH........",
    ".kkkkkHkkkkkk...",
    ".k..........k...",
    "eDDDDDDDDDDDDe..",
    "edddddddddddde..",
    "edddddddddddde..",
    "eDDDDDDDDDDDDe..",
    "ekkkkkkkkkkkke..",
    "................",
]


def write_png(path: Path, pixels: list[list[tuple[int, int, int, int]]]) -> None:
    height, width = len(pixels), len(pixels[0])
    raw = b"".join(b"\x00" + b"".join(struct.pack("4B", *px) for px in row) for row in pixels)

    def chunk(kind: bytes, data: bytes) -> bytes:
        return struct.pack(">I", len(data)) + kind + data + struct.pack(">I", zlib.crc32(kind + data) & 0xFFFFFFFF)

    png = b"\x89PNG\r\n\x1a\n"
    png += chunk(b"IHDR", struct.pack(">IIBBBBB", width, height, 8, 6, 0, 0, 0))
    png += chunk(b"IDAT", zlib.compress(raw, 9))
    png += chunk(b"IEND", b"")
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_bytes(png)


def main() -> None:
    texture = [[PALETTE[c] for c in row] for row in ROLLER]
    write_png(ASSETS / "textures/item/roller.png", texture)

    # 128x128 icon: the texture scaled 8x over a dirt-path coloured tile.
    scale, background = 8, (148, 122, 65, 255)
    icon = []
    for y in range(16 * scale):
        row = []
        for x in range(16 * scale):
            px = texture[y // scale][x // scale]
            row.append(px if px[3] else background)
        icon.append(row)
    write_png(ASSETS / "icon.png", icon)


if __name__ == "__main__":
    main()
