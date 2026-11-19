package ca.dieterlunn.trailblazer.fabric;

import ca.dieterlunn.trailblazer.Trailblazer;
import net.fabricmc.api.ModInitializer;

public final class TrailblazerFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        Trailblazer.init();
    }
}
