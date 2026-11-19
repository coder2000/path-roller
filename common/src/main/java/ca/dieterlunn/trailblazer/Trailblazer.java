package ca.dieterlunn.trailblazer;

import ca.dieterlunn.trailblazer.registry.ModDataComponents;
import ca.dieterlunn.trailblazer.registry.ModItems;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Trailblazer {
    public static final String MOD_ID = "trailblazer";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private Trailblazer() {
    }

    public static void init() {
        TrailblazerConfig.load();
        ModDataComponents.DATA_COMPONENTS.register();
        ModItems.ITEMS.register();
    }
}
