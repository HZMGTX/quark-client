package cc.quark.compat;

import net.minecraft.client.network.ClientPlayerEntity;

/**
 * Handles player API differences across Minecraft versions.
 * Add wrappers here when a player-related method is renamed or moved between versions.
 */
public final class PlayerCompat {

    private PlayerCompat() {}

    /**
     * Returns true if the player is currently using an item (eating, drinking, blocking).
     * The underlying API is stable across 1.18.2–1.21.x.
     */
    public static boolean isUsingItem(ClientPlayerEntity player) {
        return player.isUsingItem();
    }

    /**
     * Returns the hand-swing animation progress at the given tick delta.
     * Stable across 1.18.2–1.21.x.
     */
    public static float getHandSwingProgress(ClientPlayerEntity player, float tickDelta) {
        return player.getHandSwingProgress(tickDelta);
    }
}
