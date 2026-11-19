package ca.dieterlunn.pathroller;

import ca.dieterlunn.pathroller.item.RollerItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, PathRoller.MOD_ID);

    public static final RegistryObject<Item> ROLLER = ITEMS.register("roller", () -> {
        Item.Properties properties = new Item.Properties().stacksTo(1);
        int durability = PathRollerConfig.get().durability;
        if (durability > 0) {
            properties.durability(durability);
        }
        return new RollerItem(properties);
    });

    private ModItems() {
    }
}
