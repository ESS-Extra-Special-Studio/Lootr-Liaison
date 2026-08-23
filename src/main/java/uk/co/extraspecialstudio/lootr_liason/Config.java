package uk.co.extraspecialstudio.lootr_liason;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

@EventBusSubscriber(modid = Lootr_liason.MODID, bus = EventBusSubscriber.Bus.MOD)
public class Config {

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    // --- General ---
    private static final ModConfigSpec.IntValue FINALIZATION_DELAY_TICKS = BUILDER
            .comment("Ticks to wait after detecting a container before finalizing (allows worldgen to finish)")
            .defineInRange("finalization_delay_ticks", 20, 0, 400);

    private static final ModConfigSpec.IntValue MAX_FINALIZATIONS_PER_TICK = BUILDER
            .comment("Max containers to finalize per server tick")
            .defineInRange("max_finalizations_per_tick", 8, 1, 64);

    // --- Mimics (Artifacts mod) ---
    private static final ModConfigSpec.BooleanValue ENABLE_ARTIFACTS_MIMIC_COMPAT = BUILDER
            .comment("[mimics] Enable mimic compatibility for the Artifacts mod")
            .define("mimics.enable_artifacts_mimic_compat", true);

    private static final ModConfigSpec.ConfigValue<String> MIMIC_ROLL_TIMING = BUILDER
            .comment("[mimics] When to roll for mimic: 'finalization' or 'first_interact'")
            .define("mimics.mimic_roll_timing", "finalization");

    private static final ModConfigSpec.ConfigValue<List<? extends String>> EXCLUDE_CONTAINER_TYPES = BUILDER
            .comment("[mimics] Container block IDs excluded from mimic (e.g. minecraft:barrel)")
            .defineListAllowEmpty("mimics.exclude_container_types", List.of("minecraft:barrel"), Config::validateResourceId);

    private static final ModConfigSpec.BooleanValue RESPECT_ARTIFACTS_CONFIG = BUILDER
            .comment("[mimics] Respect Artifacts mod's mimic config when available")
            .define("mimics.respect_artifacts_config", true);

    private static final ModConfigSpec.IntValue MAX_MIMIC_CHECKS_PER_TICK = BUILDER
            .comment("[mimics] Max mimic evaluations per tick (when using first_interact)")
            .defineInRange("mimics.max_mimic_checks_per_tick", 8, 1, 64);

    static final ModConfigSpec SPEC = BUILDER.build();

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
        try {
            ResourceLocation.parse(s);
            return true;
        } catch (Exception e) {
            return false;
        }
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
