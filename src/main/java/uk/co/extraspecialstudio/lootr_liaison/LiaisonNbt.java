package uk.co.extraspecialstudio.lootr_liaison;

import net.minecraft.nbt.CompoundTag;

/**
 * NBT keys used by Lootr Liaison for stamps on block entities.
 * Prefix matches mod id for clarity.
 */
public final class LiaisonNbt {

    public static final String PREFIX = Lootr_liaison.MODID + ":";

    /** Set when container has been finalized (post-queue, stable). */
    public static final String FINALIZED = PREFIX + "finalized";

    /** Mimic gate: has this container been evaluated for mimic. */
    public static final String MIMIC_CHECKED = PREFIX + "mimic_checked";

    /** Mimic result: "none" | "armed" | "spawned". */
    public static final String MIMIC_RESULT = PREFIX + "mimic_result";

    public static final String MIMIC_NONE = "none";
    public static final String MIMIC_ARMED = "armed";
    public static final String MIMIC_SPAWNED = "spawned";

    /** Stamps written by 1.2.x, before the mod id spelling correction. */
    private static final String LEGACY_PREFIX = "lootr_liason:";

    private LiaisonNbt() {}

    public static boolean isFinalized(CompoundTag tag) {
        return tag.getBooleanOr(FINALIZED, false) || tag.getBooleanOr(LEGACY_PREFIX + "finalized", false);
    }

    public static boolean isMimicChecked(CompoundTag tag) {
        return tag.getBooleanOr(MIMIC_CHECKED, false) || tag.getBooleanOr(LEGACY_PREFIX + "mimic_checked", false);
    }

    public static String mimicResult(CompoundTag tag) {
        if (tag.contains(MIMIC_RESULT)) return tag.getStringOr(MIMIC_RESULT, "");
        String legacy = LEGACY_PREFIX + "mimic_result";
        return tag.contains(legacy) ? tag.getStringOr(legacy, "") : "";
    }

    public static void clearMimicStamps(CompoundTag tag) {
        tag.remove(MIMIC_CHECKED);
        tag.remove(MIMIC_RESULT);
        tag.remove(LEGACY_PREFIX + "mimic_checked");
        tag.remove(LEGACY_PREFIX + "mimic_result");
    }
}