package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventMove;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.EnumSetting;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Vec3d;

/**
 * Fly - allows the player to fly in survival mode using different techniques.
 *
 * <p>Modes:
 * <ul>
 *   <li><b>Vanilla</b>   - zeroes gravity and controls vertical motion via Space/Shift.</li>
 *   <li><b>Creative</b>  - enables the vanilla creative-flight capability flag.</li>
 *   <li><b>Glide</b>     - slow descent with full horizontal control.</li>
 *   <li><b>Packet</b>    - alternates between y+0.0625 and ground packets to confuse NCP.</li>
 *   <li><b>Bounce</b>    - uses rapid ground bouncing to maintain height.</li>
 *   <li><b>Firework</b>  - mimics firework-rocket boost packets for flight.</li>
 *   <li><b>Elytra</b>    - simulates elytra glide physics without requiring one equipped.</li>
 * </ul>
 */
public class Fly extends Module {

    public enum FlyMode {
        VANILLA, CREATIVE, GLIDE, PACKET, BOUNCE, FIREWORK, ELYTRA
    }

    private final EnumSetting<FlyMode> mode = register(new EnumSetting<>(
            "Mode", "Flying technique", FlyMode.VANILLA));

    private final DoubleSetting speed = register(new DoubleSetting(
            "Speed", "Horizontal fly speed (blocks/tick scale)", 3.0, 1.0, 10.0));

    private final DoubleSetting upSpeed = register(new DoubleSetting(
            "Up Speed", "Vertical rise/descent speed scale", 2.0, 0.5, 5.0));

    private final BoolSetting antiKick = register(new BoolSetting(
            "Anti Kick", "Periodically send onGround=true packets to prevent kick", true));

    private final BoolSetting bypassTimer = register(new BoolSetting(
            "Bypass Timer", "Use timer manipulation to reduce anti-cheat detection", false));

    // Internal state
    private int     bounceTimer   = 0;
    private int     antiKickTimer = 0;
    private boolean packetPhase   = false;
    private int     fireworkTicks = 0;

    public Fly() {
        super("Fly", "Allows flying in survival mode", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        bounceTimer   = 0;
        antiKickTimer = 0;
        packetPhase   = false;
        fireworkTicks = 0;

        if (mc.player == null) return;

        if (mode.get() == FlyMode.CREATIVE) {
            mc.player.getAbilities().allowFlying = true;
            mc.player.getAbilities().flying      = true;
            mc.player.sendAbilitiesUpdate();
        }
    }

    @Override
    public void onDisable() {
        if (mc.player == null) return;

        mc.player.getAbilities().allowFlying = false;
        mc.player.getAbilities().flying      = false;
        mc.player.sendAbilitiesUpdate();
        mc.player.setVelocity(0, 0, 0);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        // Anti-kick: every 20 ticks send a brief onGround packet
        if (antiKick.isEnabled()) {
            antiKickTimer++;
            if (antiKickTimer >= 20) {
                antiKickTimer = 0;
                sendGroundPacket(true);
            }
        }

        switch (mode.get()) {
            case VANILLA   -> handleVanillaFly();
            case CREATIVE  -> handleCreativeFly();
            case GLIDE     -> { /* handled in onMove */ }
            case PACKET    -> handlePacketFly();
            case BOUNCE    -> handleBounceFly();
            case FIREWORK  -> handleFireworkFly();
            case ELYTRA    -> handleElytraFly();
        }
    }

    @EventHandler
    public void onMove(EventMove event) {
        if (mc.player == null) return;

        FlyMode m = mode.get();
        if (m == FlyMode.VANILLA || m == FlyMode.GLIDE || m == FlyMode.PACKET
                || m == FlyMode.ELYTRA) {
            applyHorizontalSpeed(event);
        }

        if (m == FlyMode.GLIDE) {
            // Slow controlled descent
            if (event.getY() < -0.05) {
                event.setY(-0.05);
            }
        }
    }

    // -------------------------------------------------------------------------
    // Mode implementations
    // -------------------------------------------------------------------------

    private void handleVanillaFly() {
        Vec3d vel = mc.player.getVelocity();
        double y  = 0.0;

        if (mc.options.jumpKey.isPressed())  y =  upSpeed.get() * 0.1;
        if (mc.options.sneakKey.isPressed()) y = -upSpeed.get() * 0.1;

        mc.player.setVelocity(vel.x, y, vel.z);
        mc.player.fallDistance = 0;
    }

    private void handleCreativeFly() {
        if (!mc.player.getAbilities().allowFlying) {
            mc.player.getAbilities().allowFlying = true;
            mc.player.getAbilities().flying      = true;
            mc.player.sendAbilitiesUpdate();
        }
    }

    private void handlePacketFly() {
        Vec3d vel = mc.player.getVelocity();
        double y  = vel.y;

        if (mc.options.jumpKey.isPressed())       y =  upSpeed.get() * 0.1;
        else if (mc.options.sneakKey.isPressed()) y = -upSpeed.get() * 0.1;
        else                                      y =  0.0;

        mc.player.setVelocity(vel.x, y, vel.z);
        mc.player.fallDistance = 0;

        // Alternate y+0.0625 offset and ground packet each tick to confuse NCP
        if (mc.getNetworkHandler() != null) {
            if (packetPhase) {
                mc.getNetworkHandler().sendPacket(
                        new PlayerMoveC2SPacket.PositionAndOnGround(
                                mc.player.getX(),
                                mc.player.getY() + 0.0625,
                                mc.player.getZ(),
                                false));
            } else {
                sendGroundPacket(true);
            }
        }
        packetPhase = !packetPhase;
    }

    private void handleBounceFly() {
        bounceTimer++;
        if (bounceTimer >= 4) {
            bounceTimer = 0;
            if (mc.player.isOnGround()) {
                mc.player.jump();
            }
        }

        Vec3d vel = mc.player.getVelocity();
        if (mc.options.sneakKey.isPressed()) {
            mc.player.setVelocity(vel.x, -upSpeed.get() * 0.1, vel.z);
        }
        mc.player.fallDistance = 0;
    }

    private void handleFireworkFly() {
        // Simulate firework boost: periodic upward pulses like a firework rocket
        fireworkTicks++;
        Vec3d vel = mc.player.getVelocity();
        double y  = vel.y;

        if (mc.options.jumpKey.isPressed()) {
            y = upSpeed.get() * 0.15;
        } else if (mc.options.sneakKey.isPressed()) {
            y = -upSpeed.get() * 0.1;
        } else {
            // Hover with slight descent
            y = Math.max(vel.y, -0.03);
        }

        // Send firework-like boost packets every 10 ticks for server-side validity
        if (fireworkTicks % 10 == 0 && mc.getNetworkHandler() != null) {
            mc.getNetworkHandler().sendPacket(
                    new PlayerMoveC2SPacket.PositionAndOnGround(
                            mc.player.getX(), mc.player.getY() + 0.0001,
                            mc.player.getZ(), false));
        }

        mc.player.setVelocity(vel.x, y, vel.z);
        mc.player.fallDistance = 0;
    }

    private void handleElytraFly() {
        // Simulate elytra glide: gradual descent with horizontal control
        Vec3d vel = mc.player.getVelocity();
        double y  = vel.y;

        if (mc.options.jumpKey.isPressed()) {
            y = upSpeed.get() * 0.08;
        } else if (mc.options.sneakKey.isPressed()) {
            y = -upSpeed.get() * 0.08;
        } else {
            // Natural elytra descent at ~0.03 blocks/tick
            y = Math.max(vel.y - 0.005, -0.1);
        }

        mc.player.setVelocity(vel.x, y, vel.z);
        mc.player.fallDistance = 0;
    }

    // -------------------------------------------------------------------------
    // Shared helpers
    // -------------------------------------------------------------------------

    private void applyHorizontalSpeed(EventMove event) {
        float yaw    = mc.player.getYaw();
        float yawRad = (float) Math.toRadians(yaw);

        float fwd  = mc.player.input.movementForward;
        float side = mc.player.input.movementSideways;

        if (fwd == 0 && side == 0) {
            event.setX(0);
            event.setZ(0);
            return;
        }

        double len = Math.sqrt(fwd * fwd + side * side);
        fwd  /= (float) len;
        side /= (float) len;

        double s = speed.get() * 0.05;
        double x = (-Math.sin(yawRad) * fwd + Math.cos(yawRad) * side) * s;
        double z = ( Math.cos(yawRad) * fwd + Math.sin(yawRad) * side) * s;

        event.setX(x);
        event.setZ(z);
    }

    private void sendGroundPacket(boolean onGround) {
        if (mc.getNetworkHandler() == null || mc.player == null) return;
        mc.getNetworkHandler().sendPacket(
                new PlayerMoveC2SPacket.PositionAndOnGround(
                        mc.player.getX(), mc.player.getY(), mc.player.getZ(), onGround));
    }
}
