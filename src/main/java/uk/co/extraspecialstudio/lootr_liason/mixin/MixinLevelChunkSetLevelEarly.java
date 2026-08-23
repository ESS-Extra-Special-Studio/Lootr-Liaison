package uk.co.extraspecialstudio.lootr_liason.mixin;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Ensures a block entity has its level set as soon as it's registered with the chunk.
 * When a BE is added via addAndRegisterBlockEntity, we set its level at HEAD so that
 * when load() (and thus setLootTable) runs, Lootr sees a non-null level and does not
 * log "Block entity had its loot table set before its level was set".
 * Uses require = 0 so a missing inject target is soft; Shadow/Inject use default remapping.
 */
@Mixin(LevelChunk.class)
public class MixinLevelChunkSetLevelEarly {

    @Shadow
    @Final
    Level level;

    @Inject(method = "addAndRegisterBlockEntity", at = @At("HEAD"), require = 0)
    private void lootr_liason_setLevelBeforeAdd(BlockEntity blockEntity, CallbackInfo ci) {
        if (blockEntity != null && blockEntity.getLevel() == null && level != null) {
            blockEntity.setLevel(level);
        }
    }
}
