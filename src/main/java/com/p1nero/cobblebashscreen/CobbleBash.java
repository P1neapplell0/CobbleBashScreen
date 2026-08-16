package com.p1nero.cobblebashscreen;

import com.p1nero.cobblebashscreen.simulator.TrainingSimulatorMenu;
import com.p1nero.cobblebashscreen.progress.TrainingSimulatorData;
import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

@Mod(CobbleBash.MOD_ID)
public final class CobbleBash {
    public static final String MOD_ID = "cobblebash_screen";
    private static final Logger LOGGER = LogUtils.getLogger();

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
        modEventBus.addListener(CobbleBash::onAddPackFinders);
    }

    private static void onAddPackFinders(AddPackFindersEvent event) {
        if (event.getPackType() == PackType.CLIENT_RESOURCES) {
            event.addPackFinders(
                    ResourceLocation.fromNamespaceAndPath(MOD_ID, "packs/cobblebash_dialogue_resourcepack"),
                    PackType.CLIENT_RESOURCES,
                    Component.literal("CobbleBash - NPC Dialogue Localization"),
                    PackSource.BUILT_IN,
                    true,
                    Pack.Position.TOP
            );
            LOGGER.info("Registered built-in CobbleBash dialogue resource pack");
        }

        if (event.getPackType() == PackType.SERVER_DATA) {
            event.addPackFinders(
                    ResourceLocation.fromNamespaceAndPath(MOD_ID, "packs/cobblebash_dialogue_datapack"),
                    PackType.SERVER_DATA,
                    Component.literal("CobbleBash - NPC Dialogue Localization Data"),
                    PackSource.BUILT_IN,
                    true,
                    Pack.Position.TOP
            );
            LOGGER.info("Registered built-in CobbleBash dialogue data pack");
        }
    }
}
