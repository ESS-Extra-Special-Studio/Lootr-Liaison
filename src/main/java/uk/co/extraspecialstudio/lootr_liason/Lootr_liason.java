package uk.co.extraspecialstudio.lootr_liason;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.slf4j.Logger;
import uk.co.extraspecialstudio.lootr_liason.command.LootrLiaisonCommands;
import uk.co.extraspecialstudio.lootr_liason.compat.DeadLettersCompat;
import uk.co.extraspecialstudio.lootr_liason.detection.ContainerDetectionLayer;
import uk.co.extraspecialstudio.lootr_liason.logging.LootTableLevelWarningFilter;
import uk.co.extraspecialstudio.lootr_liason.mimic.MimicStabilityLayer;
import uk.co.extraspecialstudio.lootr_liason.queue.FinalizationQueue;

@Mod(Lootr_liason.MODID)
public class Lootr_liason {

    public static final String MODID = "lootr_liason";
    private static final Logger LOGGER = LogUtils.getLogger();

    public Lootr_liason(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);

        NeoForge.EVENT_BUS.register(this);
        NeoForge.EVENT_BUS.register(ContainerDetectionLayer.class);
        NeoForge.EVENT_BUS.register(FinalizationQueue.class);
        NeoForge.EVENT_BUS.register(MimicStabilityLayer.class);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
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
