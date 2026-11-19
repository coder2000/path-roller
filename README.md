# Trailblazer

A Minecraft mod that adds a **Path Roller**: hold use and walk, and it paves dirt paths along the way,
instead of clicking a shovel on every block.

Targets **Minecraft 26.3** on **Fabric** and **NeoForge** (built with Architectury). Requires
[Architectury API](https://modrinth.com/mod/architectury-api) (and Fabric API on Fabric).

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

### Recipe

```
 S      S = stick
 S      I = iron ingot
ILI     L = any log
```

## Configuration

`config/trailblazer.json` is created on first launch (server side for multiplayer):

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

Minecraft 26.3 needs **Java 25** to build. Point `JAVA_HOME` at a JDK 25, then:

```bash
./gradlew build
```

Jars end up in `fabric/build/libs/` and `neoforge/build/libs/` (use the ones without `-raw`/`-sources`).

Run the game in dev with `./gradlew :fabric:runClient` or `./gradlew :neoforge:runClient`.

## Layout

- `common/` – all gameplay code and assets, shared by both loaders
  - `paving/PathPaver.java` – footprint geometry (incl. diagonals) and block conversion
  - `item/RollerItem.java` – use/hold behaviour, width cycling, tooltip
  - `TrailblazerConfig.java` – JSON config
- `fabric/`, `neoforge/` – thin loader entrypoints and metadata
- `tools/make_textures.py` – regenerates the placeholder item texture and icon

## License

[MIT](LICENSE)
