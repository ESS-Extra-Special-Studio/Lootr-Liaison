package uk.co.extraspecialstudio.lootr_liaison.lootr;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.neoforged.fml.ModList;

/**
 * Lootr integration: we do not call into Lootr — it has no public API to "convert this chest now".
 * Lootr converts chests on its own (chunk load, first open, etc.). Our job is timing: we wait for
 * worldgen to settle, stamp the container as finalized, and rely on Lootr to convert when it normally would.
 * This avoids double-handling and aligns with our finalization queue.
 */
public final class LootrAdapter {

    /**
     * Finalization hook when Lootr is present. We only stamp the container; Lootr performs
     * the actual conversion when it sees the chest. No-op here — our value is the queue + mimic gate.
     */
    public static void finalize(Level level, BlockPos pos, RandomizableContainerBlockEntity be) {
        if (!ModList.get().isLoaded("lootr")) return;
        // Lootr converts chests on its own; we've delayed our finalization until after worldgen.
        // By the time we run, Lootr may have already converted, or will on first access.
    }
}
