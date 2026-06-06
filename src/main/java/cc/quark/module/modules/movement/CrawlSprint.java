package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.util.math.Vec3d;

public class CrawlSprint extends Module {

    private final BoolSetting enabled = register(new BoolSetting(
            "Enabled", "Maintain sprint momentum while crawling", true));

    public CrawlSprint() {
        super("CrawlSprint", "Maintains sprint momentum while crawling", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc == null || mc.player == null) return;
        if (!enabled.isEnabled()) return;
        if (!mc.player.isSwimming() && !mc.player.isCrawling()) return;

        float fwd  = mc.player.input.movementForward;
        float side = mc.player.input.movementSideways;
        if (fwd == 0 && side == 0) return;

        mc.player.setSprinting(true);

        Vec3d vel = mc.player.getVelocity();
        double hSpeed = Math.sqrt(vel.x * vel.x + vel.z * vel.z);

        if (hSpeed > 0.001 && hSpeed < 0.18) {
            double scale = 0.18 / hSpeed;
            mc.player.setVelocity(vel.x * scale, vel.y, vel.z * scale);
        }
    }
}
