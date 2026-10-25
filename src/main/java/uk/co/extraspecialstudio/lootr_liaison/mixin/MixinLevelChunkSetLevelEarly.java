package uk.co.extraspecialstudio.lootr_liaison.mixin;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Chunk.class)
public class MixinLevelChunkSetLevelEarly {
    @Shadow @Final private World level;

    @Inject(method = "addAndRegisterBlockEntity", at = @At("HEAD"), require = 0)
    private void lootr_liaison_setLevelBeforeAdd(TileEntity blockEntity, CallbackInfo ci) {
        if (blockEntity != null && blockEntity.getLevel() == null && level != null) {
            blockEntity.setLevelAndPosition(level, blockEntity.getBlockPos());
        }
    }
}