package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;

/**
 * AntiCooldown - reduces the client-side attack cooldown so the attack cooldown
 * bar appears full faster, allowing the player to attack at a higher rate.
 *
 * <p>This works by manipulating the {@code lastAttackedTicks} field on the client
 * player. The server still enforces its own cooldown, but this helps with
 * anti-cheat systems that check client-reported cooldown values.
 */
public class AntiCooldown extends Module {

    private final DoubleSetting cooldownMultiplier = register(new DoubleSetting(
            "Multiplier", "How much faster the cooldown regenerates (1 = vanilla, 2 = 2x speed)", 2.0, 1.0, 10.0));

    private final BoolSetting resetOnAttack = register(new BoolSetting(
            "Reset On Attack", "Instantly zero the cooldown after each attack", false));

    private final BoolSetting onlyWhenHolding = register(new BoolSetting(
            "Only When Holding", "Only apply when holding a sword or axe", true));

    public AntiCooldown() {
        super("AntiCooldown", "Reduces attack cooldown client-side for faster apparent attacks", Category.COMBAT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        if (onlyWhenHolding.isEnabled()) {
            boolean isWeapon = (mc.player.getMainHandStack().getItem() instanceof net.minecraft.item.SwordItem
                    || mc.player.getMainHandStack().getItem() instanceof net.minecraft.item.AxeItem);
            if (!isWeapon) return;
        }

        // Reduce the lastAttackedTicks counter faster by decrementing it each tick
        // The attack cooldown progress = min(1, (lastAttackedTicks + 0.5) / attackSpeed)
        // By reducing lastAttackedTicks we make the cooldown appear to refill faster
        int extra = (int) Math.floor(cooldownMultiplier.get() - 1.0);
        if (extra > 0) {
            // Advance the cooldown by decrementing the lastAttackedTicks tracker
            // In Fabric 1.21.1, getAttackCooldownProgress uses lastAttackedTicks
            // We simulate this by setting attack cooldown time to 0 more rapidly
            // by decrementing the ticks remaining
            for (int i = 0; i < extra; i++) {
                if (mc.player.getAttackCooldownProgress(0.5f) < 1.0f) {
                    // Force-advance the internal cooldown counter by calling a tick-forward
                    // The player tracks cooldown via 'lastAttackedTicks' decremented each tick
                    // We simulate an additional tick by directly adjusting the field
                    // Use reflection as there's no public API
                    try {
                        java.lang.reflect.Field field = net.minecraft.entity.player.PlayerEntity.class
                                .getDeclaredField("lastAttackedTicks");
                        field.setAccessible(true);
                        int current = field.getInt(mc.player);
                        if (current > 0) {
                            field.setInt(mc.player, Math.max(0, current - 1));
                        }
                    } catch (Exception ignored) {
                        // If reflection fails (e.g., due to obfuscation), this module is a no-op
                    }
                }
            }
        }

        if (resetOnAttack.isEnabled() && mc.player.hurtTime > 0) {
            try {
                java.lang.reflect.Field field = net.minecraft.entity.player.PlayerEntity.class
                        .getDeclaredField("lastAttackedTicks");
                field.setAccessible(true);
                field.setInt(mc.player, 0);
            } catch (Exception ignored) {}
        }
    }
}
