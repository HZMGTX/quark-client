package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

/**
 * AutoCobweb — automatically places a cobweb at the nearest enemy's feet
 * to slow them down.  Searches for cobwebs in the inventory, switches to
 * one if found, places it, then restores the previous slot.
 */
public class AutoCobweb extends Module {

    private final DoubleSetting range   = register(new DoubleSetting("Range",   "Range to search for enemies (blocks)", 5.0, 1.0, 10.0));
    private final IntSetting    delayMs = register(new IntSetting   ("Delay",   "Ms between cobweb placements",         600, 100, 3000));

    private final TimerUtil timer = new TimerUtil();
    private int prevSlot = -1;

    public AutoCobweb() {
        super("AutoCobweb", "Places cobwebs at nearest enemy feet to trap them", Category.COMBAT);
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
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(delayMs.get())) return;

        // Find nearest enemy
        LivingEntity target = null;
        double best = range.get();
        for (Entity e : mc.world.getEntities()) {
            if (e == mc.player) continue;
            if (!(e instanceof LivingEntity living)) continue;
            if (living.isRemoved() || living.getHealth() <= 0f) continue;
            double d = mc.player.distanceTo(e);
            if (d < best) { best = d; target = living; }
        }
        if (target == null) { restoreSlot(); return; }

        // Find cobweb in hotbar
        int cobwebSlot = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack s = mc.player.getInventory().getStack(i);
            if (s.isOf(Items.COBWEB)) { cobwebSlot = i; break; }
        }
        if (cobwebSlot == -1) { restoreSlot(); return; }

        int cur = mc.player.getInventory().selectedSlot;
        if (cur != cobwebSlot) {
            prevSlot = cur;
            mc.player.getInventory().selectedSlot = cobwebSlot;
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
