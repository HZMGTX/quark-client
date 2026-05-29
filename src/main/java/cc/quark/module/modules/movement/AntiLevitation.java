package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.Vec3d;

/**
 * AntiLevitation - fights the upward force of the Levitation status effect.
 *
 * <p>Options:
 * <ul>
 *   <li>Remove the effect outright each tick.</li>
 *   <li>Cancel the Y velocity caused by it.</li>
 *   <li>Optionally also remove Slow Falling.</li>
 * </ul>
 */
public class AntiLevitation extends Module {

    private final BoolSetting removeEffect = register(new BoolSetting(
            "Remove Effect", "Strip Levitation effect from player every tick", true));
    private final BoolSetting cancelVelocity = register(new BoolSetting(
            "Cancel Velocity", "Zero Y velocity while Levitation is active", true));
    private final DoubleSetting hoverSpeed = register(new DoubleSetting(
            "Hover Speed", "Y velocity to apply when cancelling (negative = descend, 0 = float)", 0.0, -0.5, 0.5));
    private final BoolSetting removeSlowFall = register(new BoolSetting(
            "Remove Slow Fall", "Also remove Slow Falling effect", false));

    public AntiLevitation() {
        super("AntiLevitation", "Cancel levitation from shulker bullets and other sources", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        boolean hasLevitation = mc.player.hasStatusEffect(StatusEffects.LEVITATION);
        boolean hasSlowFall   = mc.player.hasStatusEffect(StatusEffects.SLOW_FALLING);

        if (!hasLevitation && !(removeSlowFall.isEnabled() && hasSlowFall)) return;

        if (hasLevitation) {
            if (removeEffect.isEnabled()) {
                mc.player.removeStatusEffect(StatusEffects.LEVITATION);
            }
            if (cancelVelocity.isEnabled()) {
                Vec3d v = mc.player.getVelocity();
                mc.player.setVelocity(v.x, hoverSpeed.get(), v.z);
            }
        }

        if (removeSlowFall.isEnabled() && hasSlowFall) {
            mc.player.removeStatusEffect(StatusEffects.SLOW_FALLING);
        }
    }
}
