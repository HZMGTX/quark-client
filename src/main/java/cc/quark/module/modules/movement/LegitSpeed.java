package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.Vec3d;

/**
 * LegitSpeed - subtle speed boost applied every 2nd tick on ground.
 * CheckGround BoolSetting disables air use. Boost is a fixed 0.4 extra
 * blocks/tick applied to existing horizontal direction.
 */
public class LegitSpeed extends Module {

    private final BoolSetting checkGround = register(new BoolSetting(
            "Check Ground", "Only boost when on ground", true));
    private final DoubleSetting boost = register(new DoubleSetting(
            "Boost", "Speed added every 2nd tick", 0.4, 0.1, 1.0));

    private boolean applyThisTick = false;

    public LegitSpeed() {
        super("LegitSpeed", "Subtle every-other-tick ground speed boost", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        applyThisTick = false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (checkGround.isEnabled() && !mc.player.isOnGround()) return;

        float fwd  = mc.player.input.movementForward;
        float side = mc.player.input.movementSideways;
        if (fwd == 0 && side == 0) {
            applyThisTick = false;
            return;
        }

        applyThisTick = !applyThisTick;
        if (!applyThisTick) return;

        Vec3d vel = mc.player.getVelocity();
        double horizLen = Math.sqrt(vel.x * vel.x + vel.z * vel.z);
        if (horizLen <= 0.001) return;

        double newLen = horizLen + boost.get();
        double scale  = newLen / horizLen;
        mc.player.setVelocity(vel.x * scale, vel.y, vel.z * scale);
    }
}
