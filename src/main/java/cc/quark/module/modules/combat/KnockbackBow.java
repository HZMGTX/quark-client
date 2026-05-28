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
import net.minecraft.item.BowItem;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

public class KnockbackBow extends Module {

    private final DoubleSetting range = register(new DoubleSetting("Range", "Range to trigger bow shot", 8.0, 1.0, 15.0));
    private final IntSetting chargeTicks = register(new IntSetting("Charge Ticks", "Ticks to hold bow before releasing", 10, 5, 20));

    private final TimerUtil timer = new TimerUtil();
    private int holdTicks = 0;
    private boolean charging = false;
    private int bowSlot = -1;
    private int prevSlot = -1;

    public KnockbackBow() {
        super("KnockbackBow", "Charges and fires bow shots to apply knockback", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        holdTicks = 0;
        charging = false;
        bowSlot = -1;
        prevSlot = -1;
        timer.reset();
    }

    @Override
    public void onDisable() {
        if (mc.player == null) return;
        if (mc.player.isUsingItem()) {
            mc.player.stopUsingItem();
        }
        if (prevSlot >= 0 && mc.player.getInventory().selectedSlot != prevSlot) {
            mc.player.getInventory().selectedSlot = prevSlot;
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        LivingEntity target = findTarget();
        if (target == null) {
            if (charging && mc.player.isUsingItem()) {
                mc.player.stopUsingItem();
                charging = false;
                holdTicks = 0;
            }
            return;
        }

        bowSlot = findBowInHotbar();
        if (bowSlot == -1) return;

        if (mc.player.getInventory().selectedSlot != bowSlot) {
            prevSlot = mc.player.getInventory().selectedSlot;
            mc.player.getInventory().selectedSlot = bowSlot;
        }

        if (!charging) {
            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
            charging = true;
            holdTicks = 0;
        } else {
            holdTicks++;
            if (holdTicks >= chargeTicks.get()) {
                mc.player.stopUsingItem();
                charging = false;
                holdTicks = 0;
                if (prevSlot >= 0) {
                    mc.player.getInventory().selectedSlot = prevSlot;
                    prevSlot = -1;
                }
            }
        }
    }

    private LivingEntity findTarget() {
        LivingEntity best = null;
        double bestDist = Double.MAX_VALUE;
        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player) continue;
            if (!(entity instanceof LivingEntity living)) continue;
            if (living.isDead() || living.getHealth() <= 0f) continue;
            if (!(entity instanceof PlayerEntity)) continue;
            double dist = mc.player.distanceTo(entity);
            if (dist > range.get() || dist >= bestDist) continue;
            bestDist = dist;
            best = living;
        }
        return best;
    }

    private int findBowInHotbar() {
        if (mc.player == null) return -1;
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() instanceof BowItem) return i;
        }
        return -1;
    }
}
