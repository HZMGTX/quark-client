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
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

public class AutoSnowball extends Module {

    private final DoubleSetting range = register(new DoubleSetting("Range", "Target detection range", 12.0, 5.0, 20.0));
    private final IntSetting delayMs = register(new IntSetting("Delay ms", "Milliseconds between throws", 500, 200, 2000));

    private final TimerUtil timer = new TimerUtil();

    public AutoSnowball() {
        super("AutoSnowball", "Auto-throws snowballs at nearby enemies", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(delayMs.get())) return;

        int snowSlot = findSnowballSlot();
        if (snowSlot == -1) return;

        LivingEntity target = findNearestTarget();
        if (target == null) return;

        int prev = mc.player.getInventory().selectedSlot;
        mc.player.getInventory().selectedSlot = snowSlot;
        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        mc.player.swingHand(Hand.MAIN_HAND);
        mc.player.getInventory().selectedSlot = prev;
        timer.reset();
    }

    private int findSnowballSlot() {
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).isOf(Items.SNOWBALL)) return i;
        }
        return -1;
    }

    private LivingEntity findNearestTarget() {
        LivingEntity nearest = null;
        double best = range.get() * range.get();
        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player) continue;
            if (!(entity instanceof LivingEntity living)) continue;
            if (living.isDead()) continue;
            if (living instanceof PlayerEntity player && player.isSpectator()) continue;
            double distSq = mc.player.squaredDistanceTo(entity);
            if (distSq < best) {
                best = distSq;
                nearest = living;
            }
        }
        return nearest;
    }
}
