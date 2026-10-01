package uk.co.extraspecialstudio.lootr_liaison;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
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

    public Lootr_liaison(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);

        NeoForge.EVENT_BUS.register(this);
        NeoForge.EVENT_BUS.register(ContainerDetectionLayer.class);
        NeoForge.EVENT_BUS.register(FinalizationQueue.class);
        NeoForge.EVENT_BUS.register(MimicStabilityLayer.class);

        migrateLegacyConfig();
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
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
