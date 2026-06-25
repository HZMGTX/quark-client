package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.Vec3d;

public class AntiLevitation2 extends Module {

    private final BoolSetting cancel = register(new BoolSetting("Cancel", "Cancel upward levitation motion", true));

    public AntiLevitation2() {
        super("AntiLevitation2", "Cancels levitation effect from shulkers", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (!mc.player.hasStatusEffect(StatusEffects.LEVITATION)) return;
        if (!cancel.isEnabled()) return;

        Vec3d vel = mc.player.getVelocity();
        // Zero out the upward Y velocity to cancel levitation
        if (vel.y > 0) {
            mc.player.setVelocity(vel.x, 0.0, vel.z);
        }
    }
}
