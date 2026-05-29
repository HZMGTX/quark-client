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
 * AutoPearl — when HP drops below the threshold, finds an Ender Pearl in the
 * hotbar, switches to it, throws it, and restores the previous slot.
 * TimerUtil cooldown prevents spam.
 */
public class AutoPearl extends Module {

    private final DoubleSetting threshold = register(new DoubleSetting("Threshold", "HP at or below which to throw", 6.0,  1.0, 20.0));
    private final IntSetting    cooldown  = register(new IntSetting   ("Cooldown",  "Ms cooldown between throws",     3000, 500, 10000));

    private final TimerUtil timer = new TimerUtil();
    private int prevSlot = -1;

    public AutoPearl() {
        super("AutoPearl", "Automatically throws an Ender Pearl when HP is low", Category.COMBAT);
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
        if (!timer.hasReached(cooldown.get())) return;

        // Find pearl in hotbar
        int pearlSlot = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack s = mc.player.getInventory().getStack(i);
            if (s.isOf(Items.ENDER_PEARL)) { pearlSlot = i; break; }
        }
        if (pearlSlot == -1) return;

        int cur = mc.player.getInventory().selectedSlot;
        if (cur != pearlSlot) {
            prevSlot = cur;
            mc.player.getInventory().selectedSlot = pearlSlot;
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
