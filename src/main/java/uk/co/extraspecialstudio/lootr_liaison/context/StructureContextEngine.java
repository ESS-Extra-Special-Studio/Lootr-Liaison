package uk.co.extraspecialstudio.lootr_liaison.context;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Optional;

public final class StructureContextEngine {
    public static boolean isInStructure(World level, BlockPos pos) { return false; }
    public static Optional<ResourceLocation> getStructureAt(World level, BlockPos pos) { return Optional.empty(); }
    private StructureContextEngine() {}
}