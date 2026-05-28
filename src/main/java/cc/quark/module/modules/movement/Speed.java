package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventMove;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.EnumSetting;
import net.minecraft.util.math.Vec3d;

import java.util.Random;

/**
 * Speed - increases the player's movement speed using several techniques.
 *
 * <p>Modes:
 * <ul>
 *   <li><b>Vanilla</b>  - directly sets horizontal velocity each tick.</li>
 *   <li><b>Strafe</b>   - multiplies motion during the move event based on input direction.</li>
 *   <li><b>YPort</b>    - jump every other tick, apply high speed while airborne.</li>
 *   <li><b>Watchdog</b> - throttle to 0.38 BPS max with smooth acceleration.</li>
 *   <li><b>NCP Bhop</b> - small hop with speed boost every landing, NCP-safe.</li>
 * </ul>
 */
public class Speed extends Module {

    public enum SpeedMode {
        VANILLA, STRAFE, YPORT, WATCHDOG, NCP_BHOP
    }

    private final EnumSetting<SpeedMode> mode = register(new EnumSetting<>(
            "Mode", "Speed method", SpeedMode.NCP_BHOP));

    private final DoubleSetting speed = register(new DoubleSetting(
            "Speed", "Movement speed multiplier", 1.5, 1.0, 5.0));

    private final BoolSetting sprintBypass = register(new BoolSetting(
            "Sprint Bypass", "Keep sprinting even when receiving knockback", true));

    // Internal state
    private boolean wasOnGround   = false;
    private boolean yportPhase    = false;
    private int     watchdogTicks = 0;
    private double  currentSpeed  = 0.0;
    private int     ncpJumpTick   = 0;
    private final Random random   = new Random();

    public Speed() {
        super("Speed", "Increases movement speed", Category.MOVEMENT);
    }

    @Override
    public String getSuffix() {
        if (mc.player == null) return mode.get().name();
        Vec3d vel = mc.player.getVelocity();
        double bps = Math.sqrt(vel.x * vel.x + vel.z * vel.z) * 20.0;
        return mode.get().name() + " " + String.format("%.1f", bps) + " BPS";
    }

    @Override
    public void onEnable() {
        wasOnGround   = false;
        yportPhase    = false;
        watchdogTicks = 0;
        currentSpeed  = 0.0;
        ncpJumpTick   = 0;
    }

    @Override
    public void onDisable() {
        if (mc.player != null) {
            mc.player.setSprinting(false);
        }
        currentSpeed = 0.0;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (mc.player.isSneaking()) return;

        boolean moving   = isMoving();
        boolean onGround = mc.player.isOnGround();

        // Maintain sprint
        if (moving && (sprintBypass.isEnabled() || mc.player.hurtTime <= 0)) {
            mc.player.setSprinting(true);
        }

        switch (mode.get()) {

            // ------------------------------------------------------ VANILLA
            case VANILLA -> {
                if (moving) {
                    applyVanillaSpeed();
                } else {
                    currentSpeed = 0.0;
                }
            }

            // ------------------------------------------------------- YPORT
            case YPORT -> {
                if (moving) {
                    // On ground ticks: apply boost and set up for air phase
                    if (onGround) {
                        if (yportPhase) {
                            Vec3d vel = mc.player.getVelocity();
                            mc.player.setVelocity(vel.x, -0.1, vel.z);
                        } else {
                            mc.player.jump();
                        }
                        yportPhase = !yportPhase;
                        applyHorizontalBoostDirect(speed.get() * 0.28);
                    } else {
                        // Airborne: apply boosted speed
                        applyHorizontalBoostDirect(speed.get() * 0.30);
                    }
                } else {
                    yportPhase = false;
                }
            }

            // ----------------------------------------------------- WATCHDOG
            case WATCHDOG -> {
                // Throttle to 0.38 BPS max, smooth acceleration
                if (onGround && !wasOnGround && moving) {
                    mc.player.jump();
                    watchdogTicks = 0;
                } else if (!onGround && moving) {
                    watchdogTicks++;
                    double target    = Math.min(speed.get() * 0.19, 0.38);
                    currentSpeed     = currentSpeed + (target - currentSpeed) * 0.15;
                    Vec3d vel        = mc.player.getVelocity();
                    double[] dir     = getMovementDirection();
                    double hSpeed    = Math.sqrt(vel.x * vel.x + vel.z * vel.z);
                    if (hSpeed < currentSpeed) {
                        mc.player.setVelocity(
                                dir[0] * currentSpeed, vel.y, dir[1] * currentSpeed);
                    }
                } else if (onGround) {
                    watchdogTicks = 0;
                }
            }

            // ---------------------------------------------------- NCP_BHOP
            case NCP_BHOP -> {
                // Small bhop with NCP-safe speed
                if (onGround && !wasOnGround) {
                    ncpJumpTick = 0;
                }
                if (onGround && moving) {
                    // Jump on landing
                    mc.player.jump();
                    // Apply NCP-safe speed (0.287 * multiplier)
                    applyHorizontalBoostDirect(0.287 * speed.get() * 0.65);
                } else if (!onGround && moving) {
                    ncpJumpTick++;
                    // Alternate speed phases to dodge NCP
                    double ncpSpeed = (ncpJumpTick % 2 == 0)
                            ? 0.287 * speed.get() * 0.6
                            : 0.287 * speed.get() * 0.45;
                    applyHorizontalBoostDirect(ncpSpeed);
                }
            }

            default -> { /* STRAFE handled in onMove */ }
        }

        wasOnGround = onGround;
    }

    @EventHandler
    public void onMove(EventMove event) {
        if (mc.player == null) return;
        if (mode.get() != SpeedMode.STRAFE) return;
        if (!isMoving()) return;

        double[] dir = getMovementDirection();
        double   s   = speed.get() * 0.215;
        event.setX(dir[0] * s);
        event.setZ(dir[1] * s);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private void applyVanillaSpeed() {
        Vec3d vel    = mc.player.getVelocity();
        double[] dir = getMovementDirection();
        double s     = speed.get() * 0.215;
        currentSpeed = currentSpeed + (s - currentSpeed) * 0.3;
        mc.player.setVelocity(dir[0] * currentSpeed, vel.y, dir[1] * currentSpeed);
    }

    private void applyHorizontalBoostDirect(double s) {
        Vec3d vel    = mc.player.getVelocity();
        double[] dir = getMovementDirection();
        mc.player.setVelocity(dir[0] * s, vel.y, dir[1] * s);
    }

    /** Computes the normalised horizontal movement direction from player yaw and input. */
    private double[] getMovementDirection() {
        float  yaw    = mc.player.getYaw();
        float  fwd    = mc.player.input.movementForward;
        float  side   = mc.player.input.movementSideways;
        double yawRad = Math.toRadians(yaw);

        if (fwd == 0 && side == 0) {
            return new double[]{ -Math.sin(yawRad), Math.cos(yawRad) };
        }

        double len   = Math.sqrt(fwd * fwd + side * side);
        double nFwd  = fwd  / len;
        double nSide = side / len;

        double x = (-Math.sin(yawRad) * nFwd + Math.cos(yawRad) * nSide);
        double z = ( Math.cos(yawRad) * nFwd + Math.sin(yawRad) * nSide);
        return new double[]{ x, z };
    }

    private boolean isMoving() {
        return mc.player.input.movementForward != 0 || mc.player.input.movementSideways != 0;
    }
}
