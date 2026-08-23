package uk.co.extraspecialstudio.lootr_liason.mimic;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import uk.co.extraspecialstudio.lootr_liason.Config;
import uk.co.extraspecialstudio.lootr_liason.LiaisonNbt;

import java.util.Random;

/**
 * Mimic gate for mimics from the <b>Artifacts</b> mod: at most one mimic evaluation per eligible container.
 * Stamps: lootr_liason:mimic_checked, lootr_liason:mimic_result = "none"|"armed"|"spawned".
 * Option: roll at finalization (set armed) or on first_interact. Spawn is still done by Artifacts on first open.
 */
public final class MimicStabilityLayer {

    private static final Random RAND = new Random();
    /** Default mimic chance when Artifacts config is not available; Artifacts typically uses ~0.1. */
    private static final double DEFAULT_MIMIC_CHANCE = 0.1;

    /**
     * Called from FinalizationQueue when mimic_roll_timing is "finalization".
     * Roll once; set mimic_checked and mimic_result (none or armed). Spawn is done by Artifacts on first open.
     */
    public static void rollAtFinalization(Level level, BlockPos pos, RandomizableContainerBlockEntity be) {
        if (!Config.enableArtifactsMimicCompat || !ModList.get().isLoaded("artifacts")) return;
        CompoundTag tag = be.getPersistentData();
        if (tag.getBoolean(LiaisonNbt.MIMIC_CHECKED)) return;
        if (isExcluded(level, pos, be)) return;

        double chance = resolveMimicChance();
        boolean mimic = RAND.nextDouble() < chance;
        tag.putBoolean(LiaisonNbt.MIMIC_CHECKED, true);
        tag.putString(LiaisonNbt.MIMIC_RESULT, mimic ? LiaisonNbt.MIMIC_ARMED : LiaisonNbt.MIMIC_NONE);
    }

    /**
     * On first interact when mimic_roll_timing is "first_interact": roll once and stamp.
     * If already checked, we do nothing (Artifacts may still run; we can't block without their event/mixin).
     */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide()) return;
        if (!Config.enableArtifactsMimicCompat || !ModList.get().isLoaded("artifacts")) return;
        if (!"first_interact".equalsIgnoreCase(Config.mimicRollTiming)) return;

        BlockPos pos = event.getPos();
        BlockEntity be = event.getLevel().getBlockEntity(pos);
        if (!(be instanceof RandomizableContainerBlockEntity rcb)) return;

        CompoundTag tag = be.getPersistentData();
        if (tag.getBoolean(LiaisonNbt.MIMIC_CHECKED)) return; // already rolled; Artifacts will handle armed/spawned
        if (!tag.getBoolean(LiaisonNbt.FINALIZED)) return;   // not finalized yet
        if (isExcluded(event.getLevel(), pos, rcb)) return;

        double chance = resolveMimicChance();
        boolean mimic = RAND.nextDouble() < chance;
        tag.putBoolean(LiaisonNbt.MIMIC_CHECKED, true);
        tag.putString(LiaisonNbt.MIMIC_RESULT, mimic ? LiaisonNbt.MIMIC_ARMED : LiaisonNbt.MIMIC_NONE);
        // Actual spawn is done by Artifacts on first open when armed
    }

    private static boolean isExcluded(Level level, BlockPos pos, RandomizableContainerBlockEntity be) {
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(level.getBlockState(pos).getBlock());
        String idStr = id.toString();
        return Config.excludeContainerTypes.stream().anyMatch(s -> idStr.equals(s));
    }

    private static double resolveMimicChance() {
        if (Config.respectArtifactsConfig && ModList.get().isLoaded("artifacts")) {
            // TODO: read Artifacts config if they expose an API
        }
        return DEFAULT_MIMIC_CHANCE;
    }
}
