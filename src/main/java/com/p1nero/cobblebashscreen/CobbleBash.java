package com.p1nero.cobblebashscreen;

import com.p1nero.cobblebashscreen.simulator.TrainingSimulatorMenu;
import com.p1nero.cobblebashscreen.progress.TrainingSimulatorData;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(CobbleBash.MOD_ID)
public final class CobbleBash {
    public static final String MOD_ID = "cobblebash_screen";

    private static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, MOD_ID);
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<TrainingSimulatorMenu>> TRAINING_SIMULATOR_MENU =
            MENUS.register(
                    "training_simulator",
                    () -> new MenuType<>(TrainingSimulatorMenu::new, FeatureFlags.DEFAULT_FLAGS)
            );
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<TrainingSimulatorData>> TRAINING_SIMULATOR_DATA =
            ATTACHMENTS.register(
                    "training_simulator",
                    () -> AttachmentType.serializable(TrainingSimulatorData::new)
                            .copyOnDeath()
                            .build()
            );

    public CobbleBash(IEventBus modEventBus) {
        MENUS.register(modEventBus);
        ATTACHMENTS.register(modEventBus);
    }
}
