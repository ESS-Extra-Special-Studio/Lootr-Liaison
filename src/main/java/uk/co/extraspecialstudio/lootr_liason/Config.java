package uk.co.extraspecialstudio.lootr_liason;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

import java.util.List;

@Mod.EventBusSubscriber(modid = Lootr_liason.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {

    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    // --- General ---
    private static final ForgeConfigSpec.IntValue FINALIZATION_DELAY_TICKS = BUILDER
            .comment("Ticks to wait after detecting a container before finalizing (allows worldgen to finish)")
            .defineInRange("finalization_delay_ticks", 20, 0, 400);

    private static final ForgeConfigSpec.IntValue MAX_FINALIZATIONS_PER_TICK = BUILDER
            .comment("Max containers to finalize per server tick")
            .defineInRange("max_finalizations_per_tick", 8, 1, 64);

    // --- Mimics (Artifacts mod) ---
    private static final ForgeConfigSpec.BooleanValue ENABLE_ARTIFACTS_MIMIC_COMPAT = BUILDER
            .comment("[mimics] Enable mimic compatibility for the Artifacts mod")
            .define("mimics.enable_artifacts_mimic_compat", true);

    private static final ForgeConfigSpec.ConfigValue<String> MIMIC_ROLL_TIMING = BUILDER
            .comment("[mimics] When to roll for mimic: 'finalization' or 'first_interact'")
            .define("mimics.mimic_roll_timing", "finalization");

    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> EXCLUDE_CONTAINER_TYPES = BUILDER
            .comment("[mimics] Container block IDs excluded from mimic (e.g. minecraft:barrel)")
            .defineListAllowEmpty("mimics.exclude_container_types", List.of("minecraft:barrel"), Config::validateResourceId);

    private static final ForgeConfigSpec.BooleanValue RESPECT_ARTIFACTS_CONFIG = BUILDER
            .comment("[mimics] Respect Artifacts mod's mimic config when available")
            .define("mimics.respect_artifacts_config", true);

    private static final ForgeConfigSpec.IntValue MAX_MIMIC_CHECKS_PER_TICK = BUILDER
            .comment("[mimics] Max mimic evaluations per tick (when using first_interact)")
            .defineInRange("mimics.max_mimic_checks_per_tick", 8, 1, 64);

    static final ForgeConfigSpec SPEC = BUILDER.build();

    // Cached
    public static int finalizationDelayTicks;
    public static int maxFinalizationsPerTick;
    public static boolean enableArtifactsMimicCompat;
    public static String mimicRollTiming;
    public static List<String> excludeContainerTypes;
    public static boolean respectArtifactsConfig;
    public static int maxMimicChecksPerTick;

    private static boolean validateResourceId(Object obj) {
        if (!(obj instanceof String s)) return false;
        return ResourceLocation.tryParse(s) != null;
    }

    @SubscribeEvent
    static void onLoad(ModConfigEvent event) {
        finalizationDelayTicks = FINALIZATION_DELAY_TICKS.get();
        maxFinalizationsPerTick = MAX_FINALIZATIONS_PER_TICK.get();
        enableArtifactsMimicCompat = ENABLE_ARTIFACTS_MIMIC_COMPAT.get();
        mimicRollTiming = MIMIC_ROLL_TIMING.get();
        excludeContainerTypes = EXCLUDE_CONTAINER_TYPES.get().stream()
                .map(String::toString)
                .toList();
        respectArtifactsConfig = RESPECT_ARTIFACTS_CONFIG.get();
        maxMimicChecksPerTick = MAX_MIMIC_CHECKS_PER_TICK.get();
    }
}
