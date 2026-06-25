package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

/**
 * BowBomb — rapidly fires arrows by cycling the use-key at a configurable rate.
 *
 * <p>While the module is enabled and the player is holding a bow (or crossbow),
 * holding the attack button down triggers rapid-fire: the use-key is pressed and
 * released every {@code fireDelay} ticks, causing the bow to release at minimum
 * charge repeatedly. When {@code requireAttack} is enabled (default), the spam
 * only occurs while the player is actively holding the attack key.
 *
 * <p>Crossbow support: for crossbows the module charges and fires the same way;
 * after each shot the crossbow needs one full load cycle which is handled
 * automatically by keeping use pressed until the crossbow is loaded.
 */
public class BowBomb extends Module {

    private final IntSetting fireDelay = register(new IntSetting(
            "Fire Delay", "Ticks to hold the bow before releasing each shot", 4, 1, 20));

    private final BoolSetting requireAttack = register(new BoolSetting(
            "Require Attack", "Only fire when the attack key is held down", true));

    private final BoolSetting autoSwitch = register(new BoolSetting(
            "Auto Switch", "Automatically switch to bow in hotbar if not holding one", false));

    private int tickCounter = 0;
    private boolean charging = false;

    public BowBomb() {
        super("BowBomb", "Rapidly fires arrows — hold attack to spam bows", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        tickCounter = 0;
        charging    = false;
    }

    @Override
    public void onDisable() {
        if (mc.player != null) {
            mc.options.useKey.setPressed(false);
        }
        charging = false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.interactionManager == null) return;

        // Attack key gate
        if (requireAttack.isEnabled() && !mc.options.attackKey.isPressed()) {
            if (charging) {
                mc.options.useKey.setPressed(false);
                charging = false;
                tickCounter = 0;
            }
            return;
        }

        // Resolve bow hand
        Hand bowHand = findBowHand();

        if (bowHand == null && autoSwitch.isEnabled()) {
            int slot = findBowInHotbar();
            if (slot != -1) {
                mc.player.getInventory().selectedSlot = slot;
                bowHand = Hand.MAIN_HAND;
            }
        }

        if (bowHand == null) {
            mc.options.useKey.setPressed(false);
            charging = false;
            return;
        }

        tickCounter++;

        if (!charging) {
            mc.options.useKey.setPressed(true);
            charging = true;
        }

        if (tickCounter >= fireDelay.get()) {
            // Release to fire
            mc.options.useKey.setPressed(false);
            mc.interactionManager.stopUsingItem(mc.player);
            tickCounter = 0;
            charging    = false;
        }
    }

    /** Returns the hand holding a bow or crossbow, or null if neither. */
    private Hand findBowHand() {
        ItemStack main = mc.player.getMainHandStack();
        if (main.getItem() instanceof BowItem || main.getItem() instanceof CrossbowItem) {
            return Hand.MAIN_HAND;
        }
        ItemStack off = mc.player.getOffHandStack();
        if (off.getItem() instanceof BowItem || off.getItem() instanceof CrossbowItem) {
            return Hand.OFF_HAND;
        }
        return null;
    }

    /** Scans hotbar slots 0–8 for a bow; returns the slot index or -1. */
    private int findBowInHotbar() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() instanceof BowItem || stack.getItem() instanceof CrossbowItem) {
                return i;
            }
        }
        return -1;
    }
}
