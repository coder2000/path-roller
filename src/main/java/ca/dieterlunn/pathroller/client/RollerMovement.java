package ca.dieterlunn.pathroller.client;

import ca.dieterlunn.pathroller.PathRollerConfig;
import ca.dieterlunn.pathroller.item.RollerItem;
import net.minecraft.client.player.Input;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.MovementInputUpdateEvent;

/**
 * Vanilla slows players to 20% while they hold "use" on an item. Forge fires this event just before that
 * slowdown is applied, so scaling the input up here leaves the player at {@code rollingSpeed} of normal speed.
 */
public final class RollerMovement {
    private static final float VANILLA_USE_SLOWDOWN = 0.2F;

    private RollerMovement() {
    }

    public static void onMovementInput(MovementInputUpdateEvent event) {
        Player player = event.getEntity();
        if (!player.isUsingItem() || player.isPassenger() || !(player.getUseItem().getItem() instanceof RollerItem)) {
            return;
        }
        float boost = PathRollerConfig.get().rollingSpeed / VANILLA_USE_SLOWDOWN;
        Input input = event.getInput();
        input.leftImpulse *= boost;
        input.forwardImpulse *= boost;
    }
}
