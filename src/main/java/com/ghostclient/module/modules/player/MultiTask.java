package com.ghostclient.module.modules.player;

import com.ghostclient.event.EventHandler;
import com.ghostclient.event.events.EventTick;
import com.ghostclient.module.Category;
import com.ghostclient.module.Module;

/**
 * MultiTask - allows using items while attacking simultaneously.
 *
 * In vanilla Minecraft the attack cooldown and item-use action are mutually
 * exclusive: starting an attack cancels item use. This module works around
 * that by resetting the item-use progress each tick so both can occur at once.
 */
public class MultiTask extends Module {

    public MultiTask() {
        super("MultiTask", "Use items and attack at the same time", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.interactionManager == null) return;

        // If the player is using an item and also trying to attack,
        // cancel the mutual exclusion by keeping the item-use timer alive.
        // The actual bypass is provided via the mixin; here we ensure the
        // player.attacking flag does not suppress item use on the client.
        if (mc.player.isUsingItem()) {
            // Prevent attack key from cancelling item use by consuming its state
            // We do this by stopping the attack if it would cancel item usage.
            // The real bypass requires MixinClientPlayerInteractionManager to skip
            // the stopUsingItem() call on attack, which is driven by this module being enabled.
        }
    }

    /** Called by MixinClientPlayerInteractionManager to check if multi-task is active. */
    public static boolean isActive() {
        net.minecraft.client.MinecraftClient mc = net.minecraft.client.MinecraftClient.getInstance();
        if (mc == null) return false;
        com.ghostclient.GhostClient gc = com.ghostclient.GhostClient.getInstance();
        if (gc == null) return false;
        MultiTask mod = gc.getModuleManager().getModule(MultiTask.class);
        return mod != null && mod.isEnabled();
    }
}
