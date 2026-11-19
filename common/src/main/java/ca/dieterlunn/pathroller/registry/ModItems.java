package ca.dieterlunn.pathroller.registry;

import ca.dieterlunn.pathroller.PathRoller;
import ca.dieterlunn.pathroller.PathRollerConfig;
import ca.dieterlunn.pathroller.item.RollerItem;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(PathRoller.MOD_ID, Registries.ITEM);

    public static final RegistrySupplier<Item> ROLLER = ITEMS.register("roller", () -> {
        Item.Properties properties = new Item.Properties()
                .setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(PathRoller.MOD_ID, "roller")))
                .arch$tab(CreativeModeTabs.TOOLS_AND_UTILITIES)
                .stacksTo(1);
        int durability = PathRollerConfig.get().durability;
        if (durability > 0) {
            properties.durability(durability);
        }
        return new RollerItem(RollerItem.withUseEffects(properties));
    });

    private ModItems() {
    }
}
