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

    public AntiLevitation() {
        super("AntiLevitation", "Cancels levitation lift", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (!mc.player.hasStatusEffect(StatusEffects.LEVITATION)) return;
        Vec3d v = mc.player.getVelocity();
        if (v.y > 0) {
            mc.player.setVelocity(v.x, 0.0, v.z);
        }
    }
}
