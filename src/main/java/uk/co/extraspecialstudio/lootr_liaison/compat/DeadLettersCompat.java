package uk.co.extraspecialstudio.lootr_liaison.compat;

import net.minecraftforge.fml.ModList;

/**
 * Explicit compatibility gate for Dead Letters.
 * Lootr Liaison does not alter loot contents, but we expose this check so
 * compatibility state is visible in logs and easy to extend later if needed.
 */
public final class DeadLettersCompat {
    private static final String DEAD_LETTERS_MODID = "dead_letters";

    private DeadLettersCompat() {
    }

    public static boolean isPresent() {
        return ModList.get().isLoaded(DEAD_LETTERS_MODID);
    }
}
