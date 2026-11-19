# Trailblazer

A Minecraft mod that adds a **Path Roller**: hold use and walk, and it paves dirt paths along the way,
instead of clicking a shovel on every block.

This branch (`1.20.1`) targets **Minecraft 1.20.1** on **Forge 47.x** as a standalone mod with no other
dependencies. The `main` branch has the Minecraft 26.3 version for Fabric and NeoForge.

## Using the roller

| Action | Effect |
| --- | --- |
| Hold **use** and walk | Paves under and just ahead of you while you're on the ground |
| **Sneak + use** | Cycles the width: 1 → 2 → … → `maxWidth` → 1 (stored per roller) |

- The path runs the way you're **facing**, snapped to the nearest 45°, so you get all 8 directions.
  Diagonal paths are widened slightly (about width × √2 diagonal bands) so they look as wide as straight
  ones and you never have to step across a block corner.
- The path follows terrain up/down by `verticalReach` blocks.
- Grass and other replaceable plants on top are cleared. Flowers (the `trailblazer:clearable` tag) are
  cleared too and dropped as items. Both are controlled by `clearPlants`.
- Only blocks in the `trailblazer:rollable` block tag are converted (by default: grass, dirt, coarse dirt,
  podzol, mycelium, rooted dirt). You can extend the tag with a datapack.
- Respects spawn protection and other build permissions.
- Uses 1 durability per block paved; creative mode doesn't use durability.
- You keep walking at near-normal speed while rolling, instead of the usual 20% for held items.

### Recipe

```
 S      S = stick
 S      I = iron ingot
ILI     L = any log
```

## Configuration

`config/trailblazer.json` is created on first launch. Paving settings are read on the server;
`rollingSpeed` is read on each player's client.

| Key | Default | Meaning |
| --- | --- | --- |
| `defaultWidth` | `3` | Width of a freshly crafted roller |
| `maxWidth` | `7` | Largest width players can select (1–15) |
| `allowDiagonals` | `true` | `false` restricts paths to N/E/S/W |
| `verticalReach` | `1` | Blocks up/down the roller follows terrain (0–4) |
| `clearPlants` | `true` | Remove plants sitting on blocks being paved |
| `durability` | `1024` | Roller durability; `0` = unbreakable (needs restart) |
| `rollingSpeed` | `0.8` | Walk speed while rolling, as a fraction of normal (0.2–1.0) |

## Building

```bash
./gradlew build
```

Gradle runs on JDK 17+ and downloads the Java 17 toolchain ForgeGradle needs if it isn't installed.
The mod jar is `build/libs/trailblazer-1.20.1-<version>.jar`; drop it in your `mods` folder.

Run the game in dev with `./gradlew runClient`.

## Layout

- `src/main/java/ca/dieterlunn/trailblazer/`
  - `paving/PaveFootprint.java` – footprint geometry, including diagonals
  - `paving/PathPaver.java` – block conversion
  - `item/RollerItem.java` – use/hold behaviour, width cycling, tooltip
  - `client/RollerMovement.java` – removes the use-item slowdown while rolling
  - `TrailblazerConfig.java` – JSON config
- `tools/make_textures.py` – regenerates the placeholder item texture and icon

## License

[MIT](LICENSE)
