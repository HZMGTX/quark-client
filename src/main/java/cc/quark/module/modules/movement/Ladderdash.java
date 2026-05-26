package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.Vec3d;

/**
 * Ladderdash - sprint up ladders at high speed.
 */
public class Ladderdash extends Module {

    private final DoubleSetting speed = register(new DoubleSetting(
            "Speed", "Ladder climb speed", 0.4, 0.12, 0.8));

    public Ladderdash() {
        super("Ladderdash", "Fast ladder ascent", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (!mc.player.isClimbing() || !mc.player.input.jumping) return;
        Vec3d v = mc.player.getVelocity();
        mc.player.setVelocity(v.x, speed.get(), v.z);
    }
}
