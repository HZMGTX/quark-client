package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.Vec3d;

public class AntiLevitation extends Module {

    private final BoolSetting enabled = register(new BoolSetting(
            "Enabled", "Cancel levitation effect from shulker bullets", true));

    public AntiLevitation() {
        super("AntiLevitation", "Cancels the levitation effect from shulker bullets", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc == null || mc.player == null) return;
        if (!enabled.isEnabled()) return;
        if (!mc.player.hasStatusEffect(StatusEffects.LEVITATION)) return;

        mc.player.removeStatusEffect(StatusEffects.LEVITATION);
        Vec3d v = mc.player.getVelocity();
        mc.player.setVelocity(v.x, 0.0, v.z);
    }
}
