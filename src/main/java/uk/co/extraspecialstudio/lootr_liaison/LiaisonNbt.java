package uk.co.extraspecialstudio.lootr_liaison;

import net.minecraft.nbt.CompoundNBT;

public final class LiaisonNbt {
    public static final String PREFIX = Lootr_liaison.MODID + ":";
    public static final String FINALIZED = PREFIX + "finalized";
    public static final String MIMIC_CHECKED = PREFIX + "mimic_checked";
    public static final String MIMIC_RESULT = PREFIX + "mimic_result";
    public static final String MIMIC_NONE = "none";
    public static final String MIMIC_ARMED = "armed";
    public static final String MIMIC_SPAWNED = "spawned";
    private static final String LEGACY_PREFIX = "lootr_liason:";
    private LiaisonNbt() {}
    public static boolean isFinalized(CompoundNBT tag) {
        return tag.getBoolean(FINALIZED) || tag.getBoolean(LEGACY_PREFIX + "finalized");
    }
    public static boolean isMimicChecked(CompoundNBT tag) {
        return tag.getBoolean(MIMIC_CHECKED) || tag.getBoolean(LEGACY_PREFIX + "mimic_checked");
    }
    public static String mimicResult(CompoundNBT tag) {
        if (tag.contains(MIMIC_RESULT)) return tag.getString(MIMIC_RESULT);
        String legacy = LEGACY_PREFIX + "mimic_result";
        return tag.contains(legacy) ? tag.getString(legacy) : "";
    }
    public static void clearMimicStamps(CompoundNBT tag) {
        tag.remove(MIMIC_CHECKED);
        tag.remove(MIMIC_RESULT);
        tag.remove(LEGACY_PREFIX + "mimic_checked");
        tag.remove(LEGACY_PREFIX + "mimic_result");
    }
}