package uk.co.extraspecialstudio.lootr_liaison.mimic;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.LockableLootTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;
import uk.co.extraspecialstudio.lootr_liaison.Config;
import uk.co.extraspecialstudio.lootr_liaison.LiaisonNbt;

import java.util.Random;

public final class MimicStabilityLayer {
    private static final Random RAND = new Random();
    private static final double DEFAULT_MIMIC_CHANCE = 0.1;

    public static void rollAtFinalization(World level, BlockPos pos, LockableLootTileEntity be) {
        if (!Config.enableArtifactsMimicCompat || !ModList.get().isLoaded("artifacts")) return;
        CompoundNBT tag = be.getTileData();
        if (LiaisonNbt.isMimicChecked(tag)) return;
        if (isExcluded(level, pos)) return;
        boolean mimic = RAND.nextDouble() < DEFAULT_MIMIC_CHANCE;
        tag.putBoolean(LiaisonNbt.MIMIC_CHECKED, true);
        tag.putString(LiaisonNbt.MIMIC_RESULT, mimic ? LiaisonNbt.MIMIC_ARMED : LiaisonNbt.MIMIC_NONE);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getWorld().isClientSide()) return;
        if (!Config.enableArtifactsMimicCompat || !ModList.get().isLoaded("artifacts")) return;
        if (!"first_interact".equalsIgnoreCase(Config.mimicRollTiming)) return;
        BlockPos pos = event.getPos();
        TileEntity be = event.getWorld().getBlockEntity(pos);
        if (!(be instanceof LockableLootTileEntity)) return;
        CompoundNBT tag = be.getTileData();
        if (LiaisonNbt.isMimicChecked(tag)) return;
        if (!LiaisonNbt.isFinalized(tag)) return;
        if (isExcluded(event.getWorld(), pos)) return;
        boolean mimic = RAND.nextDouble() < DEFAULT_MIMIC_CHANCE;
        tag.putBoolean(LiaisonNbt.MIMIC_CHECKED, true);
        tag.putString(LiaisonNbt.MIMIC_RESULT, mimic ? LiaisonNbt.MIMIC_ARMED : LiaisonNbt.MIMIC_NONE);
    }

    private static boolean isExcluded(World level, BlockPos pos) {
        ResourceLocation id = ForgeRegistries.BLOCKS.getKey(level.getBlockState(pos).getBlock());
        if (id == null) return false;
        String idStr = id.toString();
        for (String s : Config.excludeContainerTypes) {
            if (idStr.equals(s)) return true;
        }
        return false;
    }
}