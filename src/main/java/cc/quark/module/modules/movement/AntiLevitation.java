package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.Vec3d;

/**
 * AntiLevitation - cancels the upward push from the levitation effect.
 */
public class AntiLevitation extends Module {

    private final cc.quark.setting.BoolSetting removeEffect = register(new cc.quark.setting.BoolSetting(
            "Remove Effect", "Remove levitation status effect each tick", true));

    private final cc.quark.setting.BoolSetting keepY = register(new cc.quark.setting.BoolSetting(
            "Keep Y", "Cancel upward Y velocity caused by levitation", true));

    private final cc.quark.setting.DoubleSetting fallSpeed = register(new cc.quark.setting.DoubleSetting(
            "Fall Speed", "Downward speed when levitation is active (-0 = hover)", 0.0, -0.5, 0.2));

    public AntiLevitation() {
        super("AntiLevitation", "Cancels the levitation effect from shulker bullets", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (!mc.player.hasStatusEffect(StatusEffects.LEVITATION)) return;

        if (removeEffect.isEnabled()) {
            mc.player.removeStatusEffect(StatusEffects.LEVITATION);
        }

        if (keepY.isEnabled()) {
            Vec3d v = mc.player.getVelocity();
            mc.player.setVelocity(v.x, fallSpeed.get(), v.z);
        }
    }
}
