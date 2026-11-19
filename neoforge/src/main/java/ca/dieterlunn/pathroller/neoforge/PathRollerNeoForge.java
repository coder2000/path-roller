package ca.dieterlunn.pathroller.neoforge;

import ca.dieterlunn.pathroller.PathRoller;
import net.neoforged.fml.common.Mod;

@Mod(PathRoller.MOD_ID)
public final class PathRollerNeoForge {
    public PathRollerNeoForge() {
        PathRoller.init();
    }
}
