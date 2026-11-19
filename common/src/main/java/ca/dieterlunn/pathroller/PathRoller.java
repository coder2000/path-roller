package ca.dieterlunn.pathroller;

import ca.dieterlunn.pathroller.registry.ModDataComponents;
import ca.dieterlunn.pathroller.registry.ModItems;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class PathRoller {
    public static final String MOD_ID = "pathroller";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private PathRoller() {
    }

    public static void init() {
        PathRollerConfig.load();
        ModDataComponents.DATA_COMPONENTS.register();
        ModItems.ITEMS.register();
    }
}
