package uk.co.extraspecialstudio.lootr_liaison.compat;

import net.minecraftforge.fml.ModList;

public final class DeadLettersCompat {
    public static boolean isPresent() {
        return ModList.get().isLoaded("dead_letters");
    }
}