package ca.dieterlunn.trailblazer.registry;

import ca.dieterlunn.trailblazer.Trailblazer;
import ca.dieterlunn.trailblazer.TrailblazerConfig;
import ca.dieterlunn.trailblazer.item.RollerItem;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Trailblazer.MOD_ID, Registries.ITEM);

    public static final RegistrySupplier<Item> ROLLER = ITEMS.register("roller", () -> {
        Item.Properties properties = new Item.Properties()
                .setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(Trailblazer.MOD_ID, "roller")))
                .arch$tab(CreativeModeTabs.TOOLS_AND_UTILITIES)
                .stacksTo(1);
        int durability = TrailblazerConfig.get().durability;
        if (durability > 0) {
            properties.durability(durability);
        }
        return new RollerItem(RollerItem.withUseEffects(properties));
    });

    private ModItems() {
    }
}
