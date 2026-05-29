package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventAttack;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;

import java.lang.reflect.Field;

/**
 * NoHitCooldown - zeros the player's attack cooldown field after each attack so
 * subsequent hits deal full damage immediately.
 *
 * <p>This uses reflection to reset {@code PlayerEntity.lastAttackedTicks} and
 * {@code LivingEntity.attackCooldown} (or equivalent mapped field) to 0 after
 * each attack event.  As a fallback, the tick handler also resets the field every
 * tick while the module is active.
 */
public class NoHitCooldown extends Module {

    private final BoolSetting onAttack = register(new BoolSetting(
            "On Attack", "Reset cooldown immediately after each attack event", true));

    private final BoolSetting everyTick = register(new BoolSetting(
            "Every Tick", "Also reset cooldown every tick (more aggressive)", false));

    private final DoubleSetting cooldownTarget = register(new DoubleSetting(
            "Target Cooldown", "Cooldown value to force (0 = instant ready, lower = ready sooner)", 0.0, 0.0, 0.5));

    // Cached reflection field for the attack cooldown inside LivingEntity / PlayerEntity
    private static Field attackCooldownField = null;

    static {
        // Try to find the attack cooldown field (obfuscated name will be handled by Mixin
        // mappings; here we scan by type for a float field named attackCooldown or lastAttackedTicks)
        for (Field field : net.minecraft.entity.LivingEntity.class.getDeclaredFields()) {
            if (field.getType() == float.class || field.getType() == int.class) {
                String name = field.getName();
                // Common mapped/yarn names for the attack cooldown field
                if (name.equals("attackCooldown") || name.equals("lastAttackedTicks")
                        || name.equals("field_6218") || name.equals("aq")) {
                    field.setAccessible(true);
                    attackCooldownField = field;
                    break;
                }
            }
        }
    }

    public NoHitCooldown() {
        super("NoHitCooldown", "Resets attack cooldown after each hit for instant full-damage follow-ups",
                Category.COMBAT);
    }

    @EventHandler
    public void onAttack(EventAttack event) {
        if (!onAttack.isEnabled()) return;
        resetCooldown();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (!everyTick.isEnabled() || mc.player == null) return;
        resetCooldown();
    }

    private void resetCooldown() {
        if (mc.player == null) return;

        // Primary: use vanilla method to force cooldown to the target value
        // getAttackCooldownProgress(0f) reads from the field; we reset via reflection
        if (attackCooldownField != null) {
            try {
                if (attackCooldownField.getType() == float.class) {
                    attackCooldownField.setFloat(mc.player, (float) cooldownTarget.get());
                } else if (attackCooldownField.getType() == int.class) {
                    attackCooldownField.setInt(mc.player, (int) cooldownTarget.get());
                }
            } catch (IllegalAccessException ignored) {}
        }

        // Fallback: directly set the resetLastAttackedTicks field if known
        // In 1.21.x Yarn the field is LivingEntity#attackCooldown (float)
        // and PlayerEntity#lastAttackedTicks (int).
        // The vanilla PlayerEntity.resetLastAttackedTicks() method sets it to 0.
        try {
            // Try PlayerEntity specifically
            for (Field field : mc.player.getClass().getDeclaredFields()) {
                if ((field.getType() == float.class || field.getType() == int.class)
                        && field.getName().contains("ttack")) {
                    field.setAccessible(true);
                    if (field.getType() == float.class)
                        field.setFloat(mc.player, (float) cooldownTarget.get());
                    else
                        field.setInt(mc.player, (int) cooldownTarget.get());
                }
            }
        } catch (Exception ignored) {}
    }
}
