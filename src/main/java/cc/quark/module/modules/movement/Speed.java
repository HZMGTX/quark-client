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

/**
 * Speed - increases the player's movement speed using several techniques.
 *
 * <p>Modes:
 * <ul>
 *   <li><b>Vanilla</b>  - directly sets horizontal velocity each tick via input normalization.</li>
 *   <li><b>Strafe</b>   - multiplies motion during the move event based on input direction.</li>
 *   <li><b>BHop</b>     - bunny-hop: jumps immediately on landing and boosts speed each hop.</li>
 *   <li><b>NCP</b>      - NCP-safe strafe speed (0.287) with acceleration pattern to avoid flags.</li>
 *   <li><b>Grim</b>     - conservative movement within Grim's prediction model.</li>
 *   <li><b>Matrix</b>   - gradual speed buildup matching Matrix AC expectations.</li>
 *   <li><b>AAC</b>      - ground-strafe with alternating speed pattern to avoid AAC flags.</li>
 * </ul>
 */
public class Speed extends Module {

    public enum SpeedMode {
        VANILLA, STRAFE, BHOP, NCP, GRIM, MATRIX, AAC, YPORT, WATCHDOG
    }

    private final EnumSetting<SpeedMode> mode = register(new EnumSetting<>(
            "Mode", "Speed method", SpeedMode.BHOP));

    private final DoubleSetting speed = register(new DoubleSetting(
            "Speed", "Movement speed multiplier", 1.5, 1.0, 5.0));

    private final BoolSetting acceleration = register(new BoolSetting(
            "Acceleration", "Gradually build up to target speed", true));

    private final BoolSetting sprintBypass = register(new BoolSetting(
            "Sprint Bypass", "Keep sprinting even when receiving knockback", true));

    // Internal state
    private boolean wasOnGround    = false;
    private int     ncpPhase       = 0;
    private boolean aacToggle      = false;
    private double  currentSpeed   = 0.0;
    private int     matrixTicks    = 0;
    private boolean yportPhase     = false;
    private int     watchdogTicks  = 0;

    public Speed() {
        super("Speed", "Increases movement speed", Category.MOVEMENT);
    }

    @Override
    public String getSuffix() {
        return mode.get().name();
    }

    @Override
    public void onEnable() {
        ncpPhase      = 0;
        aacToggle     = false;
        currentSpeed  = 0.0;
        matrixTicks   = 0;
        yportPhase    = false;
        watchdogTicks = 0;
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

        // Maintain sprint flag, bypassing knockback reset if enabled
        if (moving && (sprintBypass.isEnabled() || mc.player.hurtTime <= 0)) {
            mc.player.setSprinting(true);
        }

        switch (mode.get()) {
            case VANILLA -> {
                if (moving) {
                    applyVanillaSpeed();
                }
            }
            case BHOP -> {
                if (onGround && !wasOnGround && moving) {
                    mc.player.jump();
                }
                if (!onGround && moving) {
                    applyHorizontalBoost(speed.get());
                }
            }
            case NCP -> {
                // NCP-safe: use 0.287 strafe speed, alternate phases after landing
                if (onGround && !wasOnGround && moving) {
                    mc.player.jump();
                    ncpPhase = 0;
                } else if (!onGround && moving) {
                    double ncpSpeed = (ncpPhase == 0) ? 0.287 * speed.get() * 0.6
                                                      : 0.287 * speed.get() * 0.45;
                    ncpPhase = 1 - ncpPhase;
                    applyHorizontalBoostDirect(ncpSpeed);
                }
            }
            case GRIM -> {
                // Very conservative: stay just within Grim's predicted velocity window
                if (moving) {
                    double grimSpeed = 0.221 * speed.get() * 0.5;
                    applyHorizontalBoostDirect(grimSpeed);
                }
            }
            case MATRIX -> {
                // Gradual speed buildup over matrixTicks ticks
                if (moving) {
                    matrixTicks++;
                    double rampFactor = Math.min(1.0, matrixTicks / 12.0);
                    double targetSpeed = 0.26 * speed.get();
                    if (acceleration.isEnabled()) {
                        currentSpeed = currentSpeed + (targetSpeed - currentSpeed) * 0.25 * rampFactor;
                    } else {
                        currentSpeed = targetSpeed;
                    }
                    applyHorizontalBoostDirect(currentSpeed);
                } else {
                    matrixTicks = 0;
                    currentSpeed = 0.0;
                }
            }
            case AAC -> {
                if (moving) {
                    double aacSpeed = aacToggle ? speed.get() * 0.215
                                               : speed.get() * 0.215 * 0.85;
                    aacToggle = !aacToggle;
                    applyHorizontalBoostDirect(aacSpeed);
                }
            }
            case YPORT -> {
                // YPort: alternating y-position spoofing for server-side speed bypass
                // On ground ticks: apply a small downward velocity to keep "on ground" packets
                // On air ticks: apply boosted horizontal speed
                if (moving) {
                    if (onGround) {
                        // Every other ground tick, push slightly down to stay in ground detection zone
                        if (yportPhase) {
                            Vec3d vel = mc.player.getVelocity();
                            mc.player.setVelocity(vel.x, -0.1, vel.z);
                        }
                        yportPhase = !yportPhase;
                        applyHorizontalBoostDirect(speed.get() * 0.27);
                    } else {
                        applyHorizontalBoostDirect(speed.get() * 0.27);
                    }
                } else {
                    yportPhase = false;
                }
            }
            case WATCHDOG -> {
                // Hypixel Watchdog-safe bhop: jump on land with 0.42 boost, cap speed at 0.35
                if (onGround && !wasOnGround && moving) {
                    mc.player.jump();
                    watchdogTicks = 0;
                } else if (!onGround && moving) {
                    watchdogTicks++;
                    // Cap horizontal speed to 0.35 to stay under Watchdog threshold
                    double watchdogSpeed = Math.min(speed.get() * 0.215, 0.35);
                    Vec3d vel = mc.player.getVelocity();
                    double[] dir = getMovementDirection();
                    double hSpeed = Math.sqrt(vel.x * vel.x + vel.z * vel.z);
                    if (hSpeed < watchdogSpeed) {
                        mc.player.setVelocity(dir[0] * watchdogSpeed, vel.y, dir[1] * watchdogSpeed);
                    }
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
        Vec3d vel = mc.player.getVelocity();
        double[] dir = getMovementDirection();
        double s = speed.get() * 0.215;
        if (acceleration.isEnabled()) {
            currentSpeed = currentSpeed + (s - currentSpeed) * 0.3;
        } else {
            currentSpeed = s;
        }
        mc.player.setVelocity(dir[0] * currentSpeed, vel.y, dir[1] * currentSpeed);
    }

    private void applyHorizontalBoost(double multiplier) {
        Vec3d vel = mc.player.getVelocity();
        double[] dir = getMovementDirection();
        double s = multiplier * 0.215;
        if (acceleration.isEnabled()) {
            currentSpeed = currentSpeed + (s - currentSpeed) * 0.35;
        } else {
            currentSpeed = s;
        }
        mc.player.setVelocity(dir[0] * currentSpeed, vel.y, dir[1] * currentSpeed);
    }

    private void applyHorizontalBoostDirect(double s) {
        Vec3d vel = mc.player.getVelocity();
        double[] dir = getMovementDirection();
        mc.player.setVelocity(dir[0] * s, vel.y, dir[1] * s);
    }

    /** Computes the normalised horizontal movement direction from player yaw and input. */
    private double[] getMovementDirection() {
        float yaw   = mc.player.getYaw();
        float fwd   = mc.player.input.movementForward;
        float side  = mc.player.input.movementSideways;

        double yawRad = Math.toRadians(yaw);

        if (fwd == 0 && side == 0) {
            // Fall back to facing direction
            return new double[]{ -Math.sin(yawRad), Math.cos(yawRad) };
        }

        double len = Math.sqrt(fwd * fwd + side * side);
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
