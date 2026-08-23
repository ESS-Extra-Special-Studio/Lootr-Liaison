package uk.co.extraspecialstudio.lootr_liason;

/**
 * NBT keys used by Lootr Liaison for stamps on block entities.
 * Prefix matches mod id for clarity.
 */
public final class LiaisonNbt {

    public static final String PREFIX = Lootr_liason.MODID + ":";

    /** Set when container has been finalized (post-queue, stable). */
    public static final String FINALIZED = PREFIX + "finalized";

    /** Mimic gate: has this container been evaluated for mimic. */
    public static final String MIMIC_CHECKED = PREFIX + "mimic_checked";

    /** Mimic result: "none" | "armed" | "spawned". */
    public static final String MIMIC_RESULT = PREFIX + "mimic_result";

    public static final String MIMIC_NONE = "none";
    public static final String MIMIC_ARMED = "armed";
    public static final String MIMIC_SPAWNED = "spawned";

    private LiaisonNbt() {}
}
