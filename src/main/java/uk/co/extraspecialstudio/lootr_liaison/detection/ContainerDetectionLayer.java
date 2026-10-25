package uk.co.extraspecialstudio.lootr_liaison.detection;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.LockableLootTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import uk.co.extraspecialstudio.lootr_liaison.LiaisonNbt;
import uk.co.extraspecialstudio.lootr_liaison.queue.FinalizationQueue;

public final class ContainerDetectionLayer {
    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (event.getWorld().isClientSide()) return;
        if (!(event.getWorld() instanceof World)) return;
        World world = (World) event.getWorld();
        if (!(event.getChunk() instanceof Chunk)) return;
        Chunk chunk = (Chunk) event.getChunk();
        for (TileEntity be : chunk.getBlockEntities().values()) {
            if (!(be instanceof LockableLootTileEntity)) continue;
            BlockPos pos = be.getBlockPos();
            CompoundNBT tag = be.getTileData();
            if (LiaisonNbt.isFinalized(tag)) continue;
            boolean hasLootTable = tag.contains("LootTable") && !tag.getString("LootTable").isEmpty();
            boolean hasLootTableSeed = tag.contains("LootTableSeed");
            if (!hasLootTable && !hasLootTableSeed) continue;
            FinalizationQueue.enqueue(world.dimension(), pos, world.getGameTime());
        }
    }
}