package uk.co.extraspecialstudio.lootr_liaison.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.LockableLootTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import uk.co.extraspecialstudio.lootr_liaison.LiaisonNbt;

public final class LootrLiaisonCommands {
    public static void register(CommandDispatcher<CommandSource> d) {
        d.register(root("lootr_liaison"));
        d.register(root("lootr_liason"));
    }

    private static LiteralArgumentBuilder<CommandSource> root(String name) {
        return Commands.literal(name)
                .requires(s -> s.hasPermission(2))
                .then(Commands.literal("mimics")
                        .then(Commands.literal("stats").executes(c -> mimicsStats(c.getSource())))
                        .then(Commands.literal("reset")
                                .then(Commands.literal("here")
                                        .executes(ctx -> mimicsReset(ctx.getSource(), 8))
                                        .then(Commands.argument("radius", IntegerArgumentType.integer(1, 64))
                                                .executes(ctx -> mimicsReset(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "radius")))))));
    }

    private static PlayerEntity player(CommandSource src) {
        return src.getEntity() instanceof PlayerEntity ? (PlayerEntity) src.getEntity() : null;
    }

    private static int mimicsStats(CommandSource src) {
        World level = src.getLevel();
        PlayerEntity p = player(src);
        BlockPos center = p != null ? p.blockPosition() : BlockPos.ZERO;
        int chunkR = 4;
        int checked = 0, armed = 0, spawned = 0;
        int cx0 = (center.getX() >> 4) - chunkR, cx1 = (center.getX() >> 4) + chunkR;
        int cz0 = (center.getZ() >> 4) - chunkR, cz1 = (center.getZ() >> 4) + chunkR;
        for (int cx = cx0; cx <= cx1; cx++) {
            for (int cz = cz0; cz <= cz1; cz++) {
                if (!level.hasChunk(cx, cz)) continue;
                Chunk chunk = level.getChunk(cx, cz);
                for (TileEntity be : chunk.getBlockEntities().values()) {
                    if (!(be instanceof LockableLootTileEntity)) continue;
                    CompoundNBT tag = be.getTileData();
                    if (!LiaisonNbt.isMimicChecked(tag)) continue;
                    checked++;
                    String res = LiaisonNbt.mimicResult(tag);
                    if (LiaisonNbt.MIMIC_ARMED.equals(res)) armed++;
                    else if (LiaisonNbt.MIMIC_SPAWNED.equals(res)) spawned++;
                }
            }
        }
        src.sendSuccess(new StringTextComponent("Mimics: checked=" + checked + " armed=" + armed + " spawned=" + spawned), false);
        return 1;
    }

    private static int mimicsReset(CommandSource src, int radius) {
        PlayerEntity p = player(src);
        if (p == null) {
            src.sendFailure(new StringTextComponent("Can only run as a player"));
            return 0;
        }
        World level = src.getLevel();
        BlockPos center = p.blockPosition();
        int cleared = 0;
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos pos = center.offset(x, y, z);
                    TileEntity be = level.getBlockEntity(pos);
                    if (!(be instanceof LockableLootTileEntity)) continue;
                    CompoundNBT tag = be.getTileData();
                    if (!LiaisonNbt.isMimicChecked(tag)) continue;
                    LiaisonNbt.clearMimicStamps(tag);
                    be.setChanged();
                    cleared++;
                }
            }
        }
        src.sendSuccess(new StringTextComponent("Cleared mimic stamps: " + cleared + " (radius " + radius + ")"), false);
        return 1;
    }
}