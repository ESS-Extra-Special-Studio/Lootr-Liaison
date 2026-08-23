package uk.co.extraspecialstudio.lootr_liason.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import uk.co.extraspecialstudio.lootr_liason.LiaisonNbt;

/**
 * /lootr_liason mimics stats
 * /lootr_liason mimics reset here [radius]
 */
public final class LootrLiaisonCommands {

    public static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("lootr_liason")
                .requires(s -> s.hasPermission(2))
                .then(Commands.literal("mimics")
                        .then(Commands.literal("stats").executes(c -> mimicsStats(c.getSource())))
                        .then(Commands.literal("reset")
                                .then(Commands.literal("here")
                                        .executes(ctx -> mimicsReset(ctx.getSource(), 8))
                                        .then(Commands.argument("radius", IntegerArgumentType.integer(1, 64))
                                                .executes(ctx -> mimicsReset(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "radius"))))))));
    }

    private static int mimicsStats(CommandSourceStack src) {
        // Scan loaded chunks around the sender – expensive; for admin use only.
        Level level = src.getLevel();
        BlockPos center = src.getPlayer() != null ? src.getPlayer().blockPosition() : BlockPos.ZERO;
        int chunkR = 4; // 4 chunks = 64 blocks
        int checked = 0, armed = 0, spawned = 0;
        int cx0 = (center.getX() >> 4) - chunkR, cx1 = (center.getX() >> 4) + chunkR;
        int cz0 = (center.getZ() >> 4) - chunkR, cz1 = (center.getZ() >> 4) + chunkR;
        for (int cx = cx0; cx <= cx1; cx++) {
            for (int cz = cz0; cz <= cz1; cz++) {
                if (!level.hasChunk(cx, cz)) continue;
                var chunk = level.getChunk(cx, cz);
                if (!(chunk instanceof LevelChunk)) continue;
                for (BlockEntity be : ((LevelChunk) chunk).getBlockEntities().values()) {
                    if (!(be instanceof RandomizableContainerBlockEntity)) continue;
                    CompoundTag tag = be.getPersistentData();
                    if (!tag.getBoolean(LiaisonNbt.MIMIC_CHECKED)) continue;
                    checked++;
                    String res = tag.getString(LiaisonNbt.MIMIC_RESULT);
                    if (LiaisonNbt.MIMIC_ARMED.equals(res)) armed++;
                    else if (LiaisonNbt.MIMIC_SPAWNED.equals(res)) spawned++;
                }
            }
        }
        String msg = "Mimics: checked=" + checked + " armed=" + armed + " spawned=" + spawned;
        src.sendSuccess(() -> Component.literal(msg), false);
        return 1;
    }

    private static int mimicsReset(CommandSourceStack src, int radius) {
        if (src.getPlayer() == null) {
            src.sendFailure(Component.literal("Can only run as a player"));
            return 0;
        }
        Level level = src.getLevel();
        BlockPos center = src.getPlayer().blockPosition();
        int cleared = 0;
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos pos = center.offset(x, y, z);
                    BlockEntity be = level.getBlockEntity(pos);
                    if (!(be instanceof RandomizableContainerBlockEntity)) continue;
                    CompoundTag tag = be.getPersistentData();
                    if (!tag.getBoolean(LiaisonNbt.MIMIC_CHECKED)) continue;
                    tag.remove(LiaisonNbt.MIMIC_CHECKED);
                    tag.remove(LiaisonNbt.MIMIC_RESULT);
                    be.setChanged();
                    cleared++;
                }
            }
        }
        String msg = "Cleared mimic stamps: " + cleared + " (radius " + radius + ")";
        src.sendSuccess(() -> Component.literal(msg), false);
        return 1;
    }
}
