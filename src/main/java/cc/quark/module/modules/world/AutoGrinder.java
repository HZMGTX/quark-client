package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.util.Hand;

public class AutoGrinder extends Module {

    private final IntSetting attackInterval = register(new IntSetting("AttackInterval", "Milliseconds between attacks", 500, 100, 2000));
    private final IntSetting range = register(new IntSetting("Range", "Attack range in blocks", 4, 1, 6));

    private final TimerUtil timer = new TimerUtil();

    public AutoGrinder() {
        super("AutoGrinder", "Stands near mob grinder and auto-attacks spawned mobs", Category.WORLD);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(attackInterval.get())) return;

        double rangeSq = range.get() * range.get();
        LivingEntity target = null;
        double closest = Double.MAX_VALUE;

        for (var entity : mc.world.getEntities()) {
            if (!(entity instanceof LivingEntity living)) continue;
            if (living == mc.player) continue;
            if (living.isDead() || living.getHealth() <= 0) continue;
            if (!(living instanceof HostileEntity) && !(living instanceof PassiveEntity)) continue;
            double dist = mc.player.squaredDistanceTo(entity);
            if (dist > rangeSq) continue;
            if (dist < closest) {
                closest = dist;
                target = living;
            }
        }

        if (target == null) return;

        mc.interactionManager.attackEntity(mc.player, target);
        mc.player.swingHand(Hand.MAIN_HAND);
        timer.reset();
    }
}
