package uk.co.extraspecialstudio.lootr_liason;

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

    public Lootr_liason() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);

        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(ContainerDetectionLayer.class);
        MinecraftForge.EVENT_BUS.register(FinalizationQueue.class);
        MinecraftForge.EVENT_BUS.register(MimicStabilityLayer.class);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
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
