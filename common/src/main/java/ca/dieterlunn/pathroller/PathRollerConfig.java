package ca.dieterlunn.pathroller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import dev.architectury.platform.Platform;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Server-side settings, stored in {@code config/pathroller.json}.
 * Missing or invalid values fall back to the defaults below, and the file is rewritten with every key present.
 */
public final class PathRollerConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static PathRollerConfig instance = new PathRollerConfig();

    /** Width a freshly crafted roller paves. */
    public int defaultWidth = 3;
    /** Largest width a player can select by sneak-using the roller. */
    public int maxWidth = 7;
    /** Snap to 45° directions (diagonal paths). When false, only N/E/S/W. */
    public boolean allowDiagonals = true;
    /** How many blocks up/down from the player's footing the roller follows terrain. */
    public int verticalReach = 1;
    /** Remove grass, flowers and other replaceable plants sitting on top of a block being paved. */
    public boolean clearPlants = true;
    /** Total durability of the roller. 0 makes it unbreakable. Takes effect after a restart. */
    public int durability = 1024;
    /** Movement speed while rolling, as a fraction of walking speed (vanilla item use is 0.2). */
    public float rollingSpeed = 0.8F;

    public static PathRollerConfig get() {
        return instance;
    }

    public static void load() {
        Path path = Platform.getConfigFolder().resolve(PathRoller.MOD_ID + ".json");
        PathRollerConfig config = null;
        if (Files.exists(path)) {
            try (Reader reader = Files.newBufferedReader(path)) {
                config = GSON.fromJson(reader, PathRollerConfig.class);
            } catch (IOException | JsonParseException e) {
                PathRoller.LOGGER.error("Could not read {}, using defaults", path, e);
            }
        }
        if (config == null) {
            config = new PathRollerConfig();
        }
        config.sanitize();
        instance = config;

        try {
            Files.createDirectories(path.getParent());
            try (Writer writer = Files.newBufferedWriter(path)) {
                GSON.toJson(config, writer);
            }
        } catch (IOException e) {
            PathRoller.LOGGER.error("Could not write {}", path, e);
        }
    }

    private void sanitize() {
        maxWidth = clamp(maxWidth, 1, 15);
        defaultWidth = clamp(defaultWidth, 1, maxWidth);
        verticalReach = clamp(verticalReach, 0, 4);
        durability = Math.max(0, durability);
        rollingSpeed = Math.max(0.2F, Math.min(1.0F, rollingSpeed));
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
