package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.EnumSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

import java.util.Random;

public class Flight extends Module {

    public enum FlightMode {
        VANILLA, CREATIVE, PACKET, GLIDE, NCP, WATCHDOG
    }

    private final EnumSetting<FlightMode> mode = register(new EnumSetting<>(
            "Mode", "Flight mode", FlightMode.VANILLA));

    private final DoubleSetting speed = register(new DoubleSetting(
            "Speed", "Flight speed multiplier", 0.5, 0.1, 5.0));

    private final BoolSetting antiKick = register(new BoolSetting(
            "Anti-Kick", "Occasionally send onGround=true to prevent fly kick", true));

    private final IntSetting antiKickInterval = register(new IntSetting(
            "Anti-Kick Interval", "Ticks between anti-kick ground packets", 80, 20, 200));

    private int antiKickTicks = 0;
    private final Random random = new Random();

    public Flight() {
        super("Flight", "Free flight with multiple bypass modes", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        antiKickTicks = 0;
        if (mc.player != null && mode.get() == FlightMode.CREATIVE) {
            mc.player.getAbilities().allowFlying = true;
            mc.player.getAbilities().flying = true;
        }
    }

    @Override
    public void onDisable() {
        if (mc.player == null) return;
        mc.player.setVelocity(0, 0, 0);
        mc.player.getAbilities().flying = false;
        mc.player.getAbilities().allowFlying = mc.player.isCreative();
    }

    @Override
    public String getSuffix() {
        return mode.get().name();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        antiKickTicks++;
        if (antiKick.isEnabled() && antiKickTicks >= antiKickInterval.get()) {
            antiKickTicks = 0;
            sendGroundPacket(true);
        }

        double s = speed.get();
        float fwd = mc.player.input.movementForward;
        float side = mc.player.input.movementSideways;
        double yawRad = Math.toRadians(mc.player.getYaw());
        double moveX = (-Math.sin(yawRad) * fwd + Math.cos(yawRad) * side) * s;
        double moveZ = (Math.cos(yawRad) * fwd + Math.sin(yawRad) * side) * s;

        switch (mode.get()) {
            case VANILLA -> {
                double y = 0;
                if (mc.options.jumpKey.isPressed()) y += s;
                if (mc.options.sneakKey.isPressed()) y -= s;
                mc.player.setVelocity(moveX, y, moveZ);
                mc.player.fallDistance = 0;
            }

            case CREATIVE -> {
                mc.player.getAbilities().allowFlying = true;
                mc.player.getAbilities().flying = true;
                mc.player.getAbilities().setFlySpeed((float) (s * 0.05f));
                mc.player.fallDistance = 0;
            }

            case PACKET -> {
                double y;
                if (mc.options.jumpKey.isPressed()) y = s * 0.5;
                else if (mc.options.sneakKey.isPressed()) y = -s * 0.5;
                else y = -0.02;
                mc.player.setVelocity(moveX, y, moveZ);
                mc.player.fallDistance = 0;
                sendGroundPacket(false);
            }

            case GLIDE -> {
                double y = -0.03;
                if (mc.options.jumpKey.isPressed()) y = s * 0.3;
                if (mc.options.sneakKey.isPressed()) y = -s * 0.3;
                mc.player.setVelocity(moveX * 1.5, y, moveZ * 1.5);
                mc.player.fallDistance = 0;
            }

            case NCP -> {
                double ncpSpeed = Math.min(s, 0.34);
                double ncpX = (-Math.sin(yawRad) * fwd + Math.cos(yawRad) * side) * ncpSpeed;
                double ncpZ = (Math.cos(yawRad) * fwd + Math.sin(yawRad) * side) * ncpSpeed;
                double y = 0;
                if (mc.options.jumpKey.isPressed()) y = ncpSpeed * 0.5;
                if (mc.options.sneakKey.isPressed()) y = -ncpSpeed * 0.5;
                mc.player.setVelocity(ncpX, y, ncpZ);
                mc.player.fallDistance = 0;
                if (antiKickTicks % 20 == 0) sendGroundPacket(true);
            }

            case WATCHDOG -> {
                double fluctuation = (random.nextDouble() - 0.5) * 0.02;
                double y;
                if (mc.options.jumpKey.isPressed()) y = s * 0.3 + fluctuation;
                else if (mc.options.sneakKey.isPressed()) y = -s * 0.3 + fluctuation;
                else y = fluctuation;
                double wdSpeed = Math.min(s, 0.28);
                double wdX = (-Math.sin(yawRad) * fwd + Math.cos(yawRad) * side) * wdSpeed;
                double wdZ = (Math.cos(yawRad) * fwd + Math.sin(yawRad) * side) * wdSpeed;
                mc.player.setVelocity(wdX, y, wdZ);
                mc.player.fallDistance = 0;
            }
        }
    }

    private void sendGroundPacket(boolean onGround) {
        if (mc.getNetworkHandler() == null || mc.player == null) return;
        mc.getNetworkHandler().sendPacket(
                new PlayerMoveC2SPacket.PositionAndOnGround(
                        mc.player.getX(), mc.player.getY(), mc.player.getZ(), onGround));
    }
}
