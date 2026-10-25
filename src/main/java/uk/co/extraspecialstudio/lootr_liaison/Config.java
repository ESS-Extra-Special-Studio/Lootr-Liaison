package uk.co.extraspecialstudio.lootr_liaison;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(modid = Lootr_liaison.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    private static final ForgeConfigSpec.IntValue FINALIZATION_DELAY_TICKS;
    private static final ForgeConfigSpec.IntValue MAX_FINALIZATIONS_PER_TICK;
    private static final ForgeConfigSpec.BooleanValue ENABLE_ARTIFACTS_MIMIC_COMPAT;
    private static final ForgeConfigSpec.ConfigValue<String> MIMIC_ROLL_TIMING;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> EXCLUDE_CONTAINER_TYPES;
    private static final ForgeConfigSpec.BooleanValue RESPECT_ARTIFACTS_CONFIG;
    private static final ForgeConfigSpec.IntValue MAX_MIMIC_CHECKS_PER_TICK;

    static {
        BUILDER.push("general");
        FINALIZATION_DELAY_TICKS = BUILDER.defineInRange("finalization_delay_ticks", 20, 0, 400);
        MAX_FINALIZATIONS_PER_TICK = BUILDER.defineInRange("max_finalizations_per_tick", 8, 1, 64);
        BUILDER.pop();
        BUILDER.push("mimics");
        ENABLE_ARTIFACTS_MIMIC_COMPAT = BUILDER.define("enable_artifacts_mimic_compat", true);
        MIMIC_ROLL_TIMING = BUILDER.define("mimic_roll_timing", "finalization");
        EXCLUDE_CONTAINER_TYPES = BUILDER.defineList("exclude_container_types",
                new ArrayList<String>(Collections.singletonList("minecraft:barrel")), Config::validateResourceId);
        RESPECT_ARTIFACTS_CONFIG = BUILDER.define("respect_artifacts_config", true);
        MAX_MIMIC_CHECKS_PER_TICK = BUILDER.defineInRange("max_mimic_checks_per_tick", 8, 1, 64);
        BUILDER.pop();
    }

    public static final ForgeConfigSpec SPEC = BUILDER.build();
    public static int finalizationDelayTicks;
    public static int maxFinalizationsPerTick;
    public static boolean enableArtifactsMimicCompat;
    public static String mimicRollTiming;
    public static List<String> excludeContainerTypes = Collections.emptyList();
    public static boolean respectArtifactsConfig;
    public static int maxMimicChecksPerTick;

    private static boolean validateResourceId(Object obj) {
        if (!(obj instanceof String)) return false;
        try {
            new ResourceLocation((String) obj);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @SubscribeEvent
    public static void onLoad(ModConfig.ModConfigEvent event) {
        if (event.getConfig().getSpec() != SPEC) return;
        finalizationDelayTicks = FINALIZATION_DELAY_TICKS.get();
        maxFinalizationsPerTick = MAX_FINALIZATIONS_PER_TICK.get();
        enableArtifactsMimicCompat = ENABLE_ARTIFACTS_MIMIC_COMPAT.get();
        mimicRollTiming = MIMIC_ROLL_TIMING.get();
        excludeContainerTypes = EXCLUDE_CONTAINER_TYPES.get().stream().map(String::toString).collect(Collectors.toList());
        respectArtifactsConfig = RESPECT_ARTIFACTS_CONFIG.get();
        maxMimicChecksPerTick = MAX_MIMIC_CHECKS_PER_TICK.get();
    }
}