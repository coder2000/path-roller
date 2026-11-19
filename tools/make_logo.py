"""Generates the 400x400 project logo (CurseForge / Modrinth avatar): the roller on a diagonal dirt path
through a flowery meadow, as pixel art. No third-party dependencies.

Run from the repository root:  python tools/make_logo.py
"""
import random
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
from make_textures import PALETTE, ROLLER, write_png  # noqa: E402

ROOT = Path(__file__).resolve().parent.parent
OUT = ROOT / "media/logo.png"

GRID = 50          # scene size in "pixels"
SCALE = 8          # 50 * 8 = 400 px
SPRITE_SCALE = 2   # the 16x16 roller drawn at 32x32 scene pixels

GRASS = [(95, 159, 53), (86, 145, 48), (106, 170, 60), (80, 138, 45)]
GRASS_EDGE = (66, 112, 38)
PATH = [(150, 124, 66), (140, 115, 60), (160, 133, 74), (131, 107, 55)]
PATH_EDGE = (118, 96, 50)
FLOWERS = [(226, 62, 54), (250, 214, 60), (120, 150, 240), (240, 240, 245), (222, 120, 200)]
FLOWER_CENTRE = (250, 200, 60)
OUTLINE = (34, 26, 20)


def main() -> None:
    rng = random.Random(7)
    scene = [[(0, 0, 0)] * GRID for _ in range(GRID)]

    # Meadow with a diagonal path running from bottom-left to top-right.
    for y in range(GRID):
        for x in range(GRID):
            d = abs(x + y - (GRID - 1))
            if d <= 6:
                scene[y][x] = rng.choice(PATH)
            elif d == 7:
                scene[y][x] = PATH_EDGE
            elif d == 8:
                scene[y][x] = GRASS_EDGE
            else:
                scene[y][x] = rng.choice(GRASS)

    size = 16 * SPRITE_SCALE
    ox = oy = (GRID - size) // 2

    # Scatter small flowers (a 3x3 plus shape with a centre) away from the path and from under the roller.
    placed = []
    attempts = 0
    while len(placed) < 14 and attempts < 2000:
        attempts += 1
        x, y = rng.randrange(2, GRID - 2), rng.randrange(2, GRID - 2)
        under_roller = ox - 3 <= x <= ox + size + 4 and oy - 3 <= y <= oy + size + 4
        if (abs(x + y - (GRID - 1)) < 12 or under_roller
                or any(abs(x - px) + abs(y - py) < 7 for px, py in placed)):
            continue
        placed.append((x, y))
        petal = rng.choice(FLOWERS)
        for dx, dy in ((0, -1), (-1, 0), (1, 0), (0, 1)):
            scene[y + dy][x + dx] = petal
        scene[y][x] = FLOWER_CENTRE

    # The roller, centred on the path, with a drop shadow and a dark outline so it reads against the path.
    def sprite_pixels():
        for sy, row in enumerate(ROLLER):
            for sx, ch in enumerate(row):
                if PALETTE[ch][3]:
                    for ky in range(SPRITE_SCALE):
                        for kx in range(SPRITE_SCALE):
                            yield ox + sx * SPRITE_SCALE + kx, oy + sy * SPRITE_SCALE + ky, PALETTE[ch][:3]

    for x, y, _ in sprite_pixels():
        sx, sy = x + 2, y + 2
        if 0 <= sx < GRID and 0 <= sy < GRID:
            r, g, b = scene[sy][sx]
            scene[sy][sx] = (int(r * 0.6), int(g * 0.6), int(b * 0.6))
    body = {(x, y) for x, y, _ in sprite_pixels()}
    for x, y in body:
        for nx, ny in ((x + 1, y), (x - 1, y), (x, y + 1), (x, y - 1)):
            if (nx, ny) not in body and 0 <= nx < GRID and 0 <= ny < GRID:
                scene[ny][nx] = OUTLINE
    for x, y, colour in sprite_pixels():
        scene[y][x] = colour

    image = [[scene[y // SCALE][x // SCALE] + (255,) for x in range(GRID * SCALE)] for y in range(GRID * SCALE)]
    write_png(OUT, image)
    print(f"Wrote {OUT.relative_to(ROOT)}")


if __name__ == "__main__":
    main()
