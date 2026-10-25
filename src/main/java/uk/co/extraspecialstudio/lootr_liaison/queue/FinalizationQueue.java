package uk.co.extraspecialstudio.lootr_liaison.queue;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.LockableLootTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import uk.co.extraspecialstudio.lootr_liaison.Config;
import uk.co.extraspecialstudio.lootr_liaison.LiaisonNbt;
import uk.co.extraspecialstudio.lootr_liaison.lootr.LootrAdapter;
import uk.co.extraspecialstudio.lootr_liaison.mimic.MimicStabilityLayer;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class FinalizationQueue {
    private static final class Entry {
        final RegistryKey<World> dim;
        final BlockPos pos;
        final long enqueueTick;
        Entry(RegistryKey<World> dim, BlockPos pos, long enqueueTick) {
            this.dim = dim; this.pos = pos; this.enqueueTick = enqueueTick;
        }
    }
    private static final Deque<Entry> QUEUE = new ArrayDeque<Entry>();
    private static final Map<String, Boolean> SEEN = new ConcurrentHashMap<String, Boolean>();

    public static void enqueue(RegistryKey<World> dim, BlockPos pos, long currentTick) {
        String key = dim.location().toString() + "|" + pos.getX() + "," + pos.getY() + "," + pos.getZ();
        if (SEEN.putIfAbsent(key, Boolean.TRUE) != null) return;
        QUEUE.addLast(new Entry(dim, pos, currentTick));
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;
        long now = server.overworld().getGameTime();
        int delay = Config.finalizationDelayTicks;
        int max = Config.maxFinalizationsPerTick;
        int done = 0;
        while (done < max && !QUEUE.isEmpty()) {
            Entry e = QUEUE.peekFirst();
            if (e == null) break;
            if (now < e.enqueueTick + delay) break;
            QUEUE.pollFirst();
            ServerWorld level = server.getLevel(e.dim);
            if (level == null || !level.hasChunkAt(e.pos)) continue;
            TileEntity be = level.getBlockEntity(e.pos);
            if (!(be instanceof LockableLootTileEntity)) continue;
            LockableLootTileEntity rcb = (LockableLootTileEntity) be;
            CompoundNBT tag = be.getTileData();
            if (LiaisonNbt.isFinalized(tag)) continue;
            LootrAdapter.finalize(level, e.pos, rcb);
            tag.putBoolean(LiaisonNbt.FINALIZED, true);
            if (Config.enableArtifactsMimicCompat && "finalization".equalsIgnoreCase(Config.mimicRollTiming)) {
                MimicStabilityLayer.rollAtFinalization(level, e.pos, rcb);
            }
            done++;
        }
    }
}