package uk.co.extraspecialstudio.lootr_liaison;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

/**
 * Common config for Lootr Liaison ({@code config/lootr_liaison-common.toml}).
 * <p>
 * Section banners and push/pop match the RadioTowers / Dead Letters style.
 * Paths stay {@code general.*} and {@code mimics.*} for existing modpacks.
 */
@EventBusSubscriber(modid = Lootr_liaison.MODID, bus = EventBusSubscriber.Bus.MOD)
public class Config {

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    // --- General ---
    private static final ModConfigSpec.IntValue FINALIZATION_DELAY_TICKS;
    private static final ModConfigSpec.IntValue MAX_FINALIZATIONS_PER_TICK;

    // --- Mimics (Artifacts mod) ---
    private static final ModConfigSpec.BooleanValue ENABLE_ARTIFACTS_MIMIC_COMPAT;
    private static final ModConfigSpec.ConfigValue<String> MIMIC_ROLL_TIMING;
    private static final ModConfigSpec.ConfigValue<List<? extends String>> EXCLUDE_CONTAINER_TYPES;
    private static final ModConfigSpec.BooleanValue RESPECT_ARTIFACTS_CONFIG;
    private static final ModConfigSpec.IntValue MAX_MIMIC_CHECKS_PER_TICK;

    static {
        // ========== GENERAL ==========
        BUILDER.comment(
                "============================================================",
                "LOOTR FINALIZATION",
                "How long Lootr Liaison waits after spotting a container before",
                "finalizing it, and how many finalize per tick.",
                "============================================================"
        ).push("general");

        FINALIZATION_DELAY_TICKS = BUILDER
                .comment(
                        "----- START HERE: FINALIZATION -----",
                        "Ticks to wait after detecting a container before finalizing",
                        "(gives worldgen time to finish). Default: 20."
                )
                .defineInRange("finalization_delay_ticks", 20, 0, 400);
        MAX_FINALIZATIONS_PER_TICK = BUILDER
                .comment(
                        "Maximum containers to finalize per server tick.",
                        "Default: 8."
                )
                .defineInRange("max_finalizations_per_tick", 8, 1, 64);
        BUILDER.pop();

        // ========== MIMICS ==========
        BUILDER.comment(
                "============================================================",
                "MIMICS (ARTIFACTS)",
                "Compatibility with the Artifacts mod mimic chests.",
                "When Artifacts is absent these options do nothing.",
                "============================================================"
        ).push("mimics");
        ENABLE_ARTIFACTS_MIMIC_COMPAT = BUILDER
                .comment(
                        "Enable mimic compatibility for the Artifacts mod.",
                        "Default: true."
                )
                .define("enable_artifacts_mimic_compat", true);
        MIMIC_ROLL_TIMING = BUILDER
                .comment(
                        "When to roll for a mimic: \"finalization\" or \"first_interact\".",
                        "Default: finalization."
                )
                .define("mimic_roll_timing", "finalization");
        EXCLUDE_CONTAINER_TYPES = BUILDER
                .comment(
                        "Container block ids excluded from mimic rolls.",
                        "Example: minecraft:barrel. Default: minecraft:barrel only."
                )
                .defineListAllowEmpty("exclude_container_types", List.of("minecraft:barrel"), Config::validateResourceId);
        RESPECT_ARTIFACTS_CONFIG = BUILDER
                .comment(
                        "true: honour Artifacts' own mimic config when available.",
                        "Default: true."
                )
                .define("respect_artifacts_config", true);
        MAX_MIMIC_CHECKS_PER_TICK = BUILDER
                .comment(
                        "Max mimic evaluations per tick when using first_interact timing.",
                        "Default: 8."
                )
                .defineInRange("max_mimic_checks_per_tick", 8, 1, 64);
        BUILDER.pop();
    }

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
