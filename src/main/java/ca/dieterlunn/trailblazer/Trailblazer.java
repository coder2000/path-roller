package ca.dieterlunn.trailblazer;

import ca.dieterlunn.trailblazer.client.RollerMovement;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(Trailblazer.MOD_ID)
public final class Trailblazer {
    public static final String MOD_ID = "trailblazer";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public Trailblazer() {
        TrailblazerConfig.load();

        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModItems.ITEMS.register(modBus);
        modBus.addListener(Trailblazer::addToCreativeTab);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            MinecraftForge.EVENT_BUS.addListener(RollerMovement::onMovementInput);
        }
    }

    private static void addToCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(ModItems.ROLLER);
        }
    }
}
