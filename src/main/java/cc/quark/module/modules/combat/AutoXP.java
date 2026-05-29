package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

/**
 * AutoXP — periodically throws Experience Bottles from the hotbar to mend
 * equipped gear.  Optional "Stop When Full" skips when XP bar is already full.
 */
public class AutoXP extends Module {

    private final IntSetting    delayMs      = register(new IntSetting   ("Delay",          "Ms between throws",                200, 50, 2000));
    private final BoolSetting   stopWhenFull = register(new BoolSetting  ("Stop When Full", "Skip throw when XP bar is full",   true));

    private final TimerUtil timer = new TimerUtil();
    private int prevSlot = -1;

    public AutoXP() {
        super("AutoXP", "Throws Experience Bottles periodically to mend gear", Category.COMBAT);
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
        if (!timer.hasReached(delayMs.get())) return;

        // Stop if xp bar is full (experience progress >= 1.0 at max level cap is less predictable,
        // but experienceProgress == 1.0f means the current level bar is full)
        if (stopWhenFull.isEnabled() && mc.player.experienceProgress >= 1.0f) return;

        // Find XP bottle in hotbar
        int xpSlot = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack s = mc.player.getInventory().getStack(i);
            if (s.isOf(Items.EXPERIENCE_BOTTLE)) { xpSlot = i; break; }
        }
        if (xpSlot == -1) return;

        int cur = mc.player.getInventory().selectedSlot;
        if (cur != xpSlot) {
            prevSlot = cur;
            mc.player.getInventory().selectedSlot = xpSlot;
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
