package com.ghostclient.module.modules.combat;

import com.ghostclient.event.EventHandler;
import com.ghostclient.event.events.EventTick;
import com.ghostclient.module.Category;
import com.ghostclient.module.Module;
import com.ghostclient.setting.BoolSetting;
import com.ghostclient.setting.DoubleSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

/**
 * AutoGapple - automatically switches to a golden apple (or enchanted golden apple)
 * and right-clicks it when the player's health falls below a configurable threshold.
 *
 * <p>After eating, the module switches back to the previously selected hotbar slot
 * to avoid disrupting normal gameplay.
 */
public class AutoGapple extends Module {

    private final DoubleSetting health = register(new DoubleSetting(
            "Health Threshold", "Eat a gapple when health is at or below this value", 12.0, 1.0, 18.0));

    private final BoolSetting gapple = register(new BoolSetting(
            "Golden Apple", "Use regular golden apples", true));

    private final BoolSetting enchGapple = register(new BoolSetting(
            "Enchanted Gapple", "Prefer enchanted golden apples (god apples)", true));

    /** The slot we were on before auto-switching, so we can restore it. */
    private int previousSlot = -1;

    /** Whether we are currently holding a gapple and right-clicking. */
    private boolean eating = false;

    public AutoGapple() {
        super("AutoGapple", "Automatically eats golden apples when health is low", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        previousSlot = -1;
        eating = false;
    }

    @Override
    public void onDisable() {
        // Restore slot if we were mid-eat
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null && previousSlot != -1) {
            mc.player.getInventory().selectedSlot = previousSlot;
        }
        previousSlot = -1;
        eating = false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        float currentHealth = mc.player.getHealth();

        // If health recovered and we're done eating, restore slot
        if (eating && currentHealth > (float) health.get()) {
            if (previousSlot != -1) {
                mc.player.getInventory().selectedSlot = previousSlot;
                previousSlot = -1;
            }
            eating = false;
            return;
        }

        if (currentHealth > (float) health.get()) return;

        // Find the best gapple slot in the hotbar
        int slot = findGappleSlot(mc);
        if (slot == -1) {
            // No gapple available; reset state
            if (eating && previousSlot != -1) {
                mc.player.getInventory().selectedSlot = previousSlot;
                previousSlot = -1;
            }
            eating = false;
            return;
        }

        // Switch to the gapple slot if not already there
        if (mc.player.getInventory().selectedSlot != slot) {
            if (!eating) {
                previousSlot = mc.player.getInventory().selectedSlot;
            }
            mc.player.getInventory().selectedSlot = slot;
        }

        eating = true;

        // Simulate holding right-click to eat
        mc.options.useKey.setPressed(true);
        mc.interactionManager.interactItem(mc.player, net.minecraft.util.Hand.MAIN_HAND);
    }

    /**
     * Searches hotbar slots 0-8 for a golden apple.
     * Prefers enchanted golden apples when the {@code enchGapple} setting is enabled.
     * Returns -1 if none found.
     */
    private int findGappleSlot(MinecraftClient mc) {
        int regularSlot = -1;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty()) continue;

            if (enchGapple.isEnabled() && stack.getItem() == Items.ENCHANTED_GOLDEN_APPLE) {
                return i; // Enchanted gapple takes top priority
            }

            if (gapple.isEnabled() && stack.getItem() == Items.GOLDEN_APPLE && regularSlot == -1) {
                regularSlot = i;
            }
        }

        return regularSlot;
    }
}
