package ca.dieterlunn.pathroller.fabric;

import ca.dieterlunn.pathroller.PathRoller;
import net.fabricmc.api.ModInitializer;

public final class PathRollerFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        PathRoller.init();
    }
}
