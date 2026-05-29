package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.Vec3d;

/**
 * YPort - NCP-bypass speed: on the ground tick set Y=0.42 (vanilla jump
 * velocity) to exploit vertical-port; in the air apply horizontal boost.
 * TimerUtil used to enforce per-ground-contact timing.
 */
public class YPort extends Module {

    private final DoubleSetting hBoost = register(new DoubleSetting(
            "H Boost", "Horizontal multiplier while airborne", 1.18, 1.0, 2.0));

    private boolean wasOnGround = false;
    private boolean armed        = false;

    public YPort() {
        super("YPort", "NCP-bypass vertical-port speed: jump on ground, boost in air", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        wasOnGround = false;
        armed        = false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        float fwd  = mc.player.input.movementForward;
        float side = mc.player.input.movementSideways;
        if (fwd == 0 && side == 0) {
            armed = false;
            wasOnGround = mc.player.isOnGround();
            return;
        }

        boolean onGround = mc.player.isOnGround();
        Vec3d v = mc.player.getVelocity();

        if (onGround) {
            if (!armed) {
                // First tick on ground: set jump velocity
                mc.player.setVelocity(v.x, 0.42, v.z);
                armed = true;
            } else {
                armed = false;
            }
        } else if (!onGround && wasOnGround) {
            // Just left ground — apply horizontal boost for this tick
            mc.player.setVelocity(v.x * hBoost.get(), v.y, v.z * hBoost.get());
        }

        wasOnGround = onGround;
    }
}
