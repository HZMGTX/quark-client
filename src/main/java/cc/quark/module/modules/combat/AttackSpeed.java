package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.ModeSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;

import java.lang.reflect.Field;

public class AttackSpeed extends Module {

    private final ModeSetting mode = register(new ModeSetting("Mode", "Attack speed mode", "Tick", "Tick", "Timer"));
    private final DoubleSetting speed = register(new DoubleSetting("Speed", "Attacks per second", 8.0, 1.0, 20.0));

    private final TimerUtil timer = new TimerUtil();
    private Field attackCooldownField = null;
    private boolean reflectFailed = false;

    public AttackSpeed() {
        super("AttackSpeed", "Increase attack speed via tick or timer manipulation", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        timer.reset();
        tryReflect();
    }

    private void tryReflect() {
        if (reflectFailed || attackCooldownField != null) return;
        try {
            for (Class<?> cls = mc.player.getClass(); cls != null && cls != Object.class; cls = cls.getSuperclass()) {
                for (Field f : cls.getDeclaredFields()) {
                    String name = f.getName().toLowerCase();
                    if (f.getType() == int.class && (name.contains("cooldown") || name.contains("attack"))) {
                        f.setAccessible(true);
                        attackCooldownField = f;
                        return;
                    }
                }
            }
            reflectFailed = true;
        } catch (Exception e) {
            reflectFailed = true;
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        if (mode.is("Tick") && attackCooldownField != null) {
            try {
                attackCooldownField.setInt(mc.player, 0);
            } catch (Exception ignored) {}
        }

        long delay = (long) (1000.0 / speed.get());
        if (!timer.hasReached(delay)) return;

        LivingEntity target = findNearestTarget(3.5);
        if (target == null) return;

        if (mode.is("Tick") && mc.player.getAttackCooldownProgress(0f) < 0.9f) return;

        mc.interactionManager.attackEntity(mc.player, target);
        mc.player.swingHand(Hand.MAIN_HAND);
        timer.reset();
    }

    private LivingEntity findNearestTarget(double range) {
        LivingEntity nearest = null;
        double best = range * range;
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
