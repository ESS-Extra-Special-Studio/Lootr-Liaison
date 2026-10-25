package uk.co.extraspecialstudio.lootr_liaison.lootr;

import net.minecraft.tileentity.LockableLootTileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.ModList;

public final class LootrAdapter {
    public static void finalize(World level, BlockPos pos, LockableLootTileEntity be) {
        if (!ModList.get().isLoaded("lootr")) return;
    }
}