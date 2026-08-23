package uk.co.extraspecialstudio.lootr_liason.detection;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import uk.co.extraspecialstudio.lootr_liason.LiaisonNbt;
import uk.co.extraspecialstudio.lootr_liason.queue.FinalizationQueue;

/**
 * Detects structure-generated loot containers on chunk load and enqueues them for finalization.
 * Skips player-placed (no loot table from generation), already-finalized, and non-structure.
 */
public final class ContainerDetectionLayer {

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (event.getLevel().isClientSide()) return;
        Level level = (Level) event.getLevel();
        var chunk = event.getChunk();
        if (!(chunk instanceof LevelChunk lc)) return;

        for (BlockEntity be : lc.getBlockEntities().values()) {
            if (!(be instanceof RandomizableContainerBlockEntity rcb)) continue;

            BlockPos pos = be.getBlockPos();
            CompoundTag tag = be.getPersistentData();

            // Skip if already finalized by us
            if (tag.getBoolean(LiaisonNbt.FINALIZED)) continue;

            // Must look like worldgen: LootTable or LootTableSeed in NBT (structure-generated have these; player-placed typically don't)
            boolean hasLootTable = tag.contains("LootTable") && !tag.getString("LootTable").isEmpty();
            boolean hasLootTableSeed = tag.contains("LootTableSeed");
            if (!hasLootTable && !hasLootTableSeed) continue; // likely player-placed

            ResourceKey<Level> dim = level.dimension();
            FinalizationQueue.enqueue(dim, pos, level.getGameTime());
        }
    }
}
