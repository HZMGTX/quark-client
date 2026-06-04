package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.Vec3d;

public class InertiaBoost extends Module {

    private final DoubleSetting transfer = register(new DoubleSetting(
            "Transfer", "Fraction of fall speed converted to horizontal boost", 0.5, 0.0, 2.0));

    private boolean wasInAir = false;
    private double lastFallSpeed = 0.0;

    public InertiaBoost() {
        super("InertiaBoost", "Converts fall momentum into horizontal speed", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        wasInAir = false;
        lastFallSpeed = 0.0;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        boolean onGround = mc.player.isOnGround();
        Vec3d vel = mc.player.getVelocity();

        if (!onGround) {
            // Track downward velocity while falling
            if (vel.y < 0) {
                lastFallSpeed = Math.abs(vel.y);
            }
            wasInAir = true;
        } else if (wasInAir && onGround) {
            // Just landed — apply horizontal momentum burst
            if (lastFallSpeed > 0.1) {
                float yaw = mc.player.getYaw();
                float fwd = mc.player.input.movementForward;
                float side = mc.player.input.movementSideways;
                double yawRad = Math.toRadians(yaw);

                double dx, dz;
                if (fwd == 0 && side == 0) {
                    dx = -Math.sin(yawRad);
                    dz = Math.cos(yawRad);
                } else {
                    double len = Math.sqrt(fwd * fwd + side * side);
                    dx = (-Math.sin(yawRad) * (fwd / len) + Math.cos(yawRad) * (side / len));
                    dz = (Math.cos(yawRad) * (fwd / len) + Math.sin(yawRad) * (side / len));
                }

                double boost = lastFallSpeed * transfer.get();
                mc.player.setVelocity(
                        vel.x + dx * boost,
                        vel.y,
                        vel.z + dz * boost
                );
            }

            wasInAir = false;
            lastFallSpeed = 0.0;
        }
    }
}
