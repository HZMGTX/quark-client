package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.Hand;

import java.lang.reflect.Field;

public class Berserker extends Module {

    private final DoubleSetting range = register(new DoubleSetting("Range", "Attack range in blocks", 4.0, 1.0, 6.0));
    private final DoubleSetting maxBoost = register(new DoubleSetting("Max Boost", "Maximum attack speed multiplier at 1 HP", 8.0, 1.0, 10.0));
    private final BoolSetting speedBoost = register(new BoolSetting("Speed Boost", "Apply speed effect when health is below 6", true));

    private Field lastAttackedTicksField = null;

    public Berserker() {
        super("Berserker", "Increases attack speed as your health decreases", Category.COMBAT);
        try {
            for (Field f : PlayerEntity.class.getDeclaredFields()) {
                f.setAccessible(true);
                if (f.getType() == int.class) {
                    lastAttackedTicksField = f;
                }
            }
        } catch (Exception ignored) {}
        try {
            Field f = PlayerEntity.class.getDeclaredField("lastAttackedTicks");
            f.setAccessible(true);
            lastAttackedTicksField = f;
        } catch (NoSuchFieldException ignored) {}
    }

    @Override
    public void onDisable() {
        if (mc.player == null) return;
        if (mc.player.hasStatusEffect(StatusEffects.SPEED)) {
            mc.player.removeStatusEffect(StatusEffects.SPEED);
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        float health = mc.player.getHealth();
        float maxHealth = mc.player.getMaxHealth();
        float missingRatio = Math.max(0f, 1.0f - (health / maxHealth));

        if (speedBoost.isEnabled() && health < 6.0f) {
            if (!mc.player.hasStatusEffect(StatusEffects.SPEED)) {
                mc.player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 60, 1, false, false));
            }
        }

        if (lastAttackedTicksField != null) {
            try {
                double boost = 1.0 + missingRatio * (maxBoost.get() - 1.0);
                int reduction = (int) Math.floor(boost);
                int current = lastAttackedTicksField.getInt(mc.player);
                if (current > 0) {
                    lastAttackedTicksField.setInt(mc.player, Math.max(0, current - reduction));
                }
            } catch (IllegalAccessException ignored) {}
        }

        if (mc.player.getAttackCooldownProgress(0.0f) < 1.0f) return;

        LivingEntity target = null;
        double bestDist = Double.MAX_VALUE;

        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player) continue;
            if (!(entity instanceof LivingEntity living)) continue;
            if (living.isDead() || living.getHealth() <= 0f) continue;
            double dist = mc.player.distanceTo(entity);
            if (dist > range.get()) continue;
            if (dist < bestDist) {
                bestDist = dist;
                target = living;
            }
        }

        if (target != null) {
            mc.interactionManager.attackEntity(mc.player, target);
            mc.player.swingHand(Hand.MAIN_HAND);
            if (!mc.player.isSprinting()) {
                mc.player.setSprinting(true);
            }
        }
    }
}
