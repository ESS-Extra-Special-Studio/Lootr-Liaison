package uk.co.extraspecialstudio.lootr_liaison;

import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.slf4j.Logger;
import uk.co.extraspecialstudio.lootr_liaison.command.LootrLiaisonCommands;
import uk.co.extraspecialstudio.lootr_liaison.compat.DeadLettersCompat;
import uk.co.extraspecialstudio.lootr_liaison.detection.ContainerDetectionLayer;
import uk.co.extraspecialstudio.lootr_liaison.logging.LootTableLevelWarningFilter;
import uk.co.extraspecialstudio.lootr_liaison.mimic.MimicStabilityLayer;
import uk.co.extraspecialstudio.lootr_liaison.queue.FinalizationQueue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Mod(Lootr_liaison.MODID)
public class Lootr_liaison {

    public static final String MODID = "lootr_liaison";
    private static final Logger LOGGER = LogUtils.getLogger();

    public Lootr_liaison() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);

        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(ContainerDetectionLayer.class);
        MinecraftForge.EVENT_BUS.register(FinalizationQueue.class);
        MinecraftForge.EVENT_BUS.register(MimicStabilityLayer.class);

        migrateLegacyConfig();
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    /** First launch after the id correction keeps the old common config. */
    private static void migrateLegacyConfig() {
        try {
            Path dir = FMLPaths.CONFIGDIR.get();
            Path legacy = dir.resolve("lootr_liason-common.toml");
            Path current = dir.resolve(MODID + "-common.toml");
            if (Files.exists(legacy) && !Files.exists(current)) {
                Files.copy(legacy, current);
            }
        } catch (IOException e) {
            LOGGER.warn("Lootr Liaison: could not copy legacy config", e);
        }
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("Lootr Liaison: structure container finalization and mimic stability enabled.");
        if (DeadLettersCompat.isPresent()) {
            LOGGER.info("Lootr Liaison: Dead Letters detected, loot passthrough compatibility active.");
        }
        installLootTableLevelWarningFilter();
    }

    /**
     * Suppresses the "block entity had its loot table set before its level was set" warning
     * that can appear during worldgen. Safe no-op if Log4j context is unavailable.
     */
    private void installLootTableLevelWarningFilter() {
        try {
            LoggerContext ctx = LoggerContext.getContext(false);
            LoggerConfig root = ctx.getConfiguration().getRootLogger();
            root.addFilter(new LootTableLevelWarningFilter());
            ctx.updateLoggers(ctx.getConfiguration());
        } catch (Throwable t) {
            LOGGER.debug("Lootr Liaison: could not install log filter for loot-table-level warning", t);
        }
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        LootrLiaisonCommands.register(event.getDispatcher());
    }
}
