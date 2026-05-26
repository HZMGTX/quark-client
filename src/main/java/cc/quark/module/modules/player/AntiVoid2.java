package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.Vec3d;

/**
 * AntiVoid2 - halts downward velocity when approaching the void.
 */
public class AntiVoid2 extends Module {

    private final DoubleSetting minY = register(new DoubleSetting("MinY", "Y level to trigger", 5.0, -64.0, 64.0));

    public AntiVoid2() {
        super("AntiVoid2", "Stops you from falling into the void", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (mc.player.getY() <= minY.get()) {
            Vec3d v = mc.player.getVelocity();
            mc.player.setVelocity(v.x, 0.0, v.z);
        }
    }
}
