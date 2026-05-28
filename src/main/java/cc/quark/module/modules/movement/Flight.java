package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.EnumSetting;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

import java.util.Random;

public class Flight extends Module {

    public enum FlightMode {
        VANILLA, CREATIVE, PACKET, GLIDE, NCP, WATCHDOG
    }

    private final EnumSetting<FlightMode> mode = register(new EnumSetting<>(
            "Mode", "Flight mode", FlightMode.VANILLA));

    private final DoubleSetting speed = register(new DoubleSetting(
            "Speed", "Flight speed multiplier", 1.0, 0.1, 10.0));

    private final DoubleSetting fallSpeed = register(new DoubleSetting(
            "Fall Speed", "Downward glide rate (Glide mode)", 0.05, 0.0, 1.0));

    private final BoolSetting antiKick = register(new BoolSetting(
            "Anti Kick", "Periodically send onGround=true to prevent fly-kick", true));

    // Internal state
    private int    tickCounter   = 0;
    private boolean wasFlying    = false;
    private boolean savedAllowFly;
    private boolean savedFlying;
    private boolean savedCreative;
    private float   savedFlySpeed;
    private final Random random  = new Random();

    public Flight() {
        super("Flight", "Free flight with multiple bypass modes", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        tickCounter = 0;
        if (mc.player == null) return;
        // Save abilities so we can restore on disable
        savedAllowFly  = mc.player.getAbilities().allowFlying;
        savedFlying    = mc.player.getAbilities().flying;
        savedFlySpeed  = mc.player.getAbilities().getFlySpeed();

        switch (mode.get()) {
            case VANILLA -> {
                mc.player.getAbilities().allowFlying = true;
                mc.player.getAbilities().flying = true;
            }
            case CREATIVE -> {
                mc.player.getAbilities().allowFlying = true;
                mc.player.getAbilities().flying = true;
            }
            default -> {}
        }
    }

    @Override
    public void onDisable() {
        if (mc.player == null) return;
        // Restore abilities
        mc.player.getAbilities().allowFlying = savedAllowFly;
        mc.player.getAbilities().flying      = savedFlying;
        mc.player.getAbilities().setFlySpeed(savedFlySpeed);
        mc.player.setVelocity(0, 0, 0);
        mc.player.fallDistance = 0;
    }

    @Override
    public String getSuffix() {
        return mode.get().name() + " " + String.format("%.1f", speed.get());
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        tickCounter++;
        double s      = speed.get();
        float  fwd    = mc.player.input.movementForward;
        float  side   = mc.player.input.movementSideways;
        double yawRad = Math.toRadians(mc.player.getYaw());
        double moveX  = (-Math.sin(yawRad) * fwd + Math.cos(yawRad) * side) * s;
        double moveZ  = ( Math.cos(yawRad) * fwd + Math.sin(yawRad) * side) * s;

        switch (mode.get()) {

            // -------------------------------------------------------- VANILLA
            case VANILLA -> {
                mc.player.getAbilities().allowFlying = true;
                mc.player.getAbilities().flying = true;

                double y = 0;
                if (mc.options.jumpKey.isPressed())  y += s;
                if (mc.options.sneakKey.isPressed()) y -= s;
                mc.player.setVelocity(moveX, y, moveZ);
                mc.player.fallDistance = 0;

                // Anti-kick: random small Y dip every ~40 ticks
                if (antiKick.isEnabled() && tickCounter % 40 == 0) {
                    mc.player.setVelocity(
                            mc.player.getVelocity().x,
                            mc.player.getVelocity().y - (random.nextDouble() * 0.04),
                            mc.player.getVelocity().z);
                }
            }

            // ------------------------------------------------------- CREATIVE
            case CREATIVE -> {
                mc.player.getAbilities().allowFlying = true;
                mc.player.getAbilities().flying = true;
                mc.player.getAbilities().setFlySpeed((float)(s * 0.05f));
                mc.player.fallDistance = 0;

                if (antiKick.isEnabled() && tickCounter % 40 == 0) {
                    // Briefly zero velocity to trick anti-kick
                    mc.player.setVelocity(
                            mc.player.getVelocity().x,
                            mc.player.getVelocity().y - (random.nextDouble() * 0.03),
                            mc.player.getVelocity().z);
                }
            }

            // --------------------------------------------------------- PACKET
            case PACKET -> {
                // Stay "on ground" server-side by sending Y+0.0625 offset packets
                double y;
                if (mc.options.jumpKey.isPressed())       y = s * 0.5;
                else if (mc.options.sneakKey.isPressed()) y = -s * 0.5;
                else                                      y = -0.02;

                mc.player.setVelocity(moveX, y, moveZ);
                mc.player.fallDistance = 0;

                // Send a ground-spoofed packet each tick
                sendPositionPacket(false);

                // Anti-kick Y dip every 20 ticks
                if (antiKick.isEnabled() && tickCounter % 20 == 0) {
                    sendPositionPacketAt(
                            mc.player.getX(),
                            mc.player.getY() - 0.0625,
                            mc.player.getZ(),
                            true);
                }
            }

            // ---------------------------------------------------------- GLIDE
            case GLIDE -> {
                double glideDown = -fallSpeed.get();
                double y;
                if (mc.options.jumpKey.isPressed())       y = s * 0.3;
                else if (mc.options.sneakKey.isPressed()) y = -s * 0.3;
                else                                      y = glideDown;

                mc.player.setVelocity(moveX * 1.5, y, moveZ * 1.5);
                mc.player.fallDistance = 0;

                if (antiKick.isEnabled() && tickCounter % 40 == 0) {
                    mc.player.setVelocity(
                            mc.player.getVelocity().x,
                            mc.player.getVelocity().y - (random.nextDouble() * 0.02),
                            mc.player.getVelocity().z);
                }
            }

            // ------------------------------------------------------------- NCP
            case NCP -> {
                // Alternate Y+0.4 / Y-0.6 every tick to confuse NCP ground checks
                double ncpSpeed = Math.min(s, 0.34);
                double ncpX = (-Math.sin(yawRad) * fwd + Math.cos(yawRad) * side) * ncpSpeed;
                double ncpZ = ( Math.cos(yawRad) * fwd + Math.sin(yawRad) * side) * ncpSpeed;

                boolean evenTick = (tickCounter % 2 == 0);
                double  ncpY;
                if (mc.options.jumpKey.isPressed())       ncpY = evenTick ? 0.4 : -0.2;
                else if (mc.options.sneakKey.isPressed()) ncpY = evenTick ? -0.3 : -0.6;
                else                                      ncpY = evenTick ? 0.4 : -0.6;

                mc.player.setVelocity(ncpX, ncpY, ncpZ);
                mc.player.fallDistance = 0;

                // Send alternating ground packets
                if (antiKick.isEnabled()) {
                    sendPositionPacketAt(
                            mc.player.getX(),
                            mc.player.getY() + (evenTick ? 0.0625 : -0.0625),
                            mc.player.getZ(),
                            evenTick);
                }
            }

            // -------------------------------------------------------- WATCHDOG
            case WATCHDOG -> {
                double fluctuation = (random.nextDouble() - 0.5) * 0.02;
                double wdSpeed = Math.min(s, 0.4);
                double wdX = (-Math.sin(yawRad) * fwd + Math.cos(yawRad) * side) * wdSpeed;
                double wdZ = ( Math.cos(yawRad) * fwd + Math.sin(yawRad) * side) * wdSpeed;

                double y;
                if (mc.options.jumpKey.isPressed())       y = wdSpeed * 0.3 + fluctuation;
                else if (mc.options.sneakKey.isPressed()) y = -wdSpeed * 0.3 + fluctuation;
                else                                      y = fluctuation;

                mc.player.setVelocity(wdX, y, wdZ);
                mc.player.fallDistance = 0;

                if (antiKick.isEnabled() && tickCounter % 30 == 0) {
                    sendPositionPacketAt(
                            mc.player.getX(),
                            mc.player.getY() - 0.01,
                            mc.player.getZ(),
                            true);
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private void sendPositionPacket(boolean onGround) {
        if (mc.getNetworkHandler() == null || mc.player == null) return;
        mc.getNetworkHandler().sendPacket(
                new PlayerMoveC2SPacket.PositionAndOnGround(
                        mc.player.getX(), mc.player.getY(), mc.player.getZ(), onGround));
    }

    private void sendPositionPacketAt(double x, double y, double z, boolean onGround) {
        if (mc.getNetworkHandler() == null) return;
        mc.getNetworkHandler().sendPacket(
                new PlayerMoveC2SPacket.PositionAndOnGround(x, y, z, onGround));
    }
}
