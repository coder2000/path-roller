package ca.dieterlunn.trailblazer.registry;

import ca.dieterlunn.trailblazer.Trailblazer;
import com.mojang.serialization.Codec;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;

public final class ModDataComponents {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS =
            DeferredRegister.create(Trailblazer.MOD_ID, Registries.DATA_COMPONENT_TYPE);

    /** The path width selected on a roller. Absent means "use the configured default". */
    public static final RegistrySupplier<DataComponentType<Integer>> ROLLER_WIDTH = DATA_COMPONENTS.register("roller_width",
            () -> DataComponentType.<Integer>builder()
                    .persistent(Codec.intRange(1, 15))
                    .networkSynchronized(ByteBufCodecs.VAR_INT)
                    .build());

    private ModDataComponents() {
    }
}
