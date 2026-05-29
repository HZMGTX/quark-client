package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

/**
 * AutoSoup — when HP drops below the threshold, finds a mushroom soup in the
 * hotbar, switches to it, right-clicks to eat, then restores the previous slot.
 */
public class AutoSoup extends Module {

    private final DoubleSetting threshold = register(new DoubleSetting("Threshold", "HP at or below which to eat soup",  15.0, 1.0, 20.0));
    private final IntSetting    delayMs   = register(new IntSetting   ("Delay",     "Ms between soup uses",              200, 50, 1000));

    private final TimerUtil timer = new TimerUtil();
    private int prevSlot = -1;

    public AutoSoup() {
        super("AutoSoup", "Automatically eats mushroom soup when HP is low", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        timer.reset();
        prevSlot = -1;
    }

    @Override
    public void onDisable() {
        restoreSlot();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.interactionManager == null) return;
        if (mc.player.getHealth() > threshold.get()) { restoreSlot(); return; }
        if (!timer.hasReached(delayMs.get())) return;

        // Find soup (mushroom stew) in hotbar
        int soupSlot = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack s = mc.player.getInventory().getStack(i);
            if (s.isOf(Items.MUSHROOM_STEW) || s.isOf(Items.SUSPICIOUS_STEW)) {
                soupSlot = i;
                break;
            }
        }
        if (soupSlot == -1) return;

        int cur = mc.player.getInventory().selectedSlot;
        if (cur != soupSlot) {
            prevSlot = cur;
            mc.player.getInventory().selectedSlot = soupSlot;
        }

        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        mc.player.swingHand(Hand.MAIN_HAND);
        timer.reset();
        restoreSlot();
    }

    private void restoreSlot() {
        if (prevSlot != -1 && mc.player != null) {
            mc.player.getInventory().selectedSlot = prevSlot;
            prevSlot = -1;
        }
    }
}
