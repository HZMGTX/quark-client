package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

/**
 * AutoGapple2 - improved golden apple auto-eater with configurable HP threshold
 * and NotchApple (enchanted golden apple) preference when HP is critically low.
 */
public class AutoGapple2 extends Module {

    private final DoubleSetting hpThreshold = register(new DoubleSetting(
            "HP Threshold", "Eat a gapple when health drops at or below this value", 8.0, 1.0, 19.0));

    private final DoubleSetting notchAppleThreshold = register(new DoubleSetting(
            "Notch HP Threshold", "Prefer enchanted golden apple when HP is at or below this value", 4.0, 1.0, 10.0));

    private final BoolSetting preferNotchApple = register(new BoolSetting(
            "Prefer NotchApple", "Use enchanted golden apples when HP is critically low", true));

    private final BoolSetting stopAtFull = register(new BoolSetting(
            "Stop At Full", "Stop eating once HP is restored above threshold", true));

    private final DoubleSetting eatDelay = register(new DoubleSetting(
            "Eat Delay ms", "Delay between eat actions in ms", 400.0, 50.0, 1000.0));

    private final TimerUtil timer = new TimerUtil();
    private int previousSlot = -1;
    private boolean eating = false;

    public AutoGapple2() {
        super("AutoGapple2", "Improved AutoGapple with NotchApple preference and configurable thresholds",
                Category.COMBAT);
    }

    @Override
    public void onEnable() {
        previousSlot = -1;
        eating = false;
        timer.reset();
    }

    @Override
    public void onDisable() {
        if (mc.player != null && previousSlot != -1) {
            mc.player.getInventory().selectedSlot = previousSlot;
        }
        previousSlot = -1;
        eating = false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(eatDelay.get())) return;

        float hp    = mc.player.getHealth();
        float maxHp = mc.player.getMaxHealth();
        float threshold = (float)(hpThreshold.get() * (maxHp / 20.0));

        // Stop eating if HP is back above threshold
        if (eating && stopAtFull.isEnabled() && hp > threshold) {
            restoreSlot();
            return;
        }

        if (hp > threshold) return;

        // Determine which apple to prefer
        boolean criticallyLow = preferNotchApple.isEnabled()
                && hp <= (float)(notchAppleThreshold.get() * (maxHp / 20.0));

        int slot = findAppleSlot(criticallyLow);
        if (slot == -1) {
            restoreSlot();
            return;
        }

        if (mc.player.getInventory().selectedSlot != slot) {
            if (!eating) previousSlot = mc.player.getInventory().selectedSlot;
            mc.player.getInventory().selectedSlot = slot;
        }

        eating = true;
        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        timer.reset();
    }

    private void restoreSlot() {
        if (previousSlot != -1) {
            mc.player.getInventory().selectedSlot = previousSlot;
            previousSlot = -1;
        }
        eating = false;
    }

    /**
     * Find the best apple slot.
     *
     * @param preferEnchanted  when true, enchanted golden apples are searched first
     */
    private int findAppleSlot(boolean preferEnchanted) {
        int enchantedSlot = -1;
        int regularSlot   = -1;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty()) continue;
            if (stack.isOf(Items.ENCHANTED_GOLDEN_APPLE) && enchantedSlot == -1) enchantedSlot = i;
            if (stack.isOf(Items.GOLDEN_APPLE) && regularSlot == -1)             regularSlot   = i;
        }

        if (preferEnchanted && enchantedSlot != -1) return enchantedSlot;
        if (regularSlot   != -1) return regularSlot;
        if (enchantedSlot != -1) return enchantedSlot;
        return -1;
    }
}
