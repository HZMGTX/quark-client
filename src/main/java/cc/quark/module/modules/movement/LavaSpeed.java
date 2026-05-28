package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventMove;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.Vec3d;

public class LavaSpeed extends Module {

    private final DoubleSetting multiplier = register(new DoubleSetting(
            "Multiplier", "Lava speed multiplier", 1.3, 1.0, 3.0));

    public LavaSpeed() {
        super("LavaSpeed", "Faster lava travel", Category.MOVEMENT);
    }

    @EventHandler
    public void onMove(EventMove event) {
        if (mc.player == null) return;
        if (!mc.player.isInLava()) return;

        Vec3d vel = mc.player.getVelocity();
        double hLen = Math.sqrt(vel.x * vel.x + vel.z * vel.z);
        if (hLen > 0) {
            double boosted = hLen * multiplier.get();
            double scale = boosted / hLen;
            mc.player.setVelocity(vel.x * scale, vel.y, vel.z * scale);
        }
    }
}
