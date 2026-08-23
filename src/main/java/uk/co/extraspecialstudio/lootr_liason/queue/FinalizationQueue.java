package uk.co.extraspecialstudio.lootr_liason.queue;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.server.ServerLifecycleHooks;
import uk.co.extraspecialstudio.lootr_liason.Config;
import uk.co.extraspecialstudio.lootr_liason.LiaisonNbt;
import uk.co.extraspecialstudio.lootr_liason.lootr.LootrAdapter;
import uk.co.extraspecialstudio.lootr_liason.mimic.MimicStabilityLayer;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Queue of (dimension, pos, enqueueTick). Processes up to maxFinalizationsPerTick each server tick.
 * Only finalizes when currentTick >= enqueueTick + finalizationDelayTicks.
 */
public final class FinalizationQueue {

    private static final class Entry {
        final ResourceKey<Level> dim;
        final BlockPos pos;
        final long enqueueTick;

        Entry(ResourceKey<Level> dim, BlockPos pos, long enqueueTick) {
            this.dim = dim;
            this.pos = pos;
            this.enqueueTick = enqueueTick;
        }
    }

    private static final Deque<Entry> QUEUE = new ArrayDeque<>();
    private static final Map<String, Boolean> SEEN = new ConcurrentHashMap<>();

    public static void enqueue(ResourceKey<Level> dim, BlockPos pos, long currentTick) {
        String key = dim.location().toString() + "|" + pos.toShortString();
        if (SEEN.putIfAbsent(key, true) != null) return; // already enqueued
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
            if (now < e.enqueueTick + delay) break; // still waiting; rest of queue is older so also waiting
            QUEUE.pollFirst();

            Level level = server.getLevel(e.dim);
            if (level == null || !level.isLoaded(e.pos)) continue;

            BlockEntity be = level.getBlockEntity(e.pos);
            if (!(be instanceof RandomizableContainerBlockEntity rcb)) continue;

            CompoundTag tag = be.getPersistentData();
            if (tag.getBoolean(LiaisonNbt.FINALIZED)) continue;

            // Finalize: hand to Lootr (adapter may no-op if Lootr does it itself) and stamp
            LootrAdapter.finalize(level, e.pos, rcb);

            tag.putBoolean(LiaisonNbt.FINALIZED, true);

            // Mimic: if roll at finalization, do it now
            if (Config.enableArtifactsMimicCompat && "finalization".equalsIgnoreCase(Config.mimicRollTiming)) {
                MimicStabilityLayer.rollAtFinalization(level, e.pos, rcb);
            }
            done++;
        }
    }
}
