package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.ModeSetting;

public class Fly extends Module {

    private final DoubleSetting speed = register(new DoubleSetting(
            "Speed", "Fly speed", 0.15, 0.01, 2.0));

    private final ModeSetting mode = register(new ModeSetting(
            "Mode", "Flying mode", "Vanilla", "Vanilla", "Packet", "Glide"));

    public Fly() {
        super("Fly", "Lets the player fly in survival", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        if (mc.player == null) return;
        if (mode.is("Vanilla")) {
            mc.player.getAbilities().allowFlying = true;
            mc.player.getAbilities().flying = true;
            mc.player.sendAbilitiesUpdate();
        }
    }

    @Override
    public void onDisable() {
        if (mc.player == null) return;
        mc.player.getAbilities().allowFlying = false;
        mc.player.getAbilities().flying = false;
        mc.player.sendAbilitiesUpdate();
        mc.player.setVelocity(0, 0, 0);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        if (mode.is("Vanilla")) {
            mc.player.getAbilities().allowFlying = true;
            mc.player.getAbilities().flying = true;

            double spd = speed.get();
            float yaw = (float) Math.toRadians(mc.player.getYaw());
            double vx = 0, vz = 0, vy = 0;

            float fwd  = mc.player.input.movementForward;
            float side = mc.player.input.movementSideways;

            if (fwd != 0 || side != 0) {
                double len = Math.sqrt(fwd * fwd + side * side);
                fwd  /= (float) len;
                side /= (float) len;
                vx = (-Math.sin(yaw) * fwd + Math.cos(yaw) * side) * spd;
                vz = ( Math.cos(yaw) * fwd + Math.sin(yaw) * side) * spd;
            }

            if (mc.options.jumpKey.isPressed())  vy =  spd;
            if (mc.options.sneakKey.isPressed()) vy = -spd;

            mc.player.setVelocity(vx, vy, vz);
            mc.player.fallDistance = 0;

        } else if (mode.is("Packet")) {
            double spd = speed.get();
            float yaw = (float) Math.toRadians(mc.player.getYaw());
            double vx = 0, vz = 0, vy = 0;

            float fwd  = mc.player.input.movementForward;
            float side = mc.player.input.movementSideways;

            if (fwd != 0 || side != 0) {
                double len = Math.sqrt(fwd * fwd + side * side);
                fwd  /= (float) len;
                side /= (float) len;
                vx = (-Math.sin(yaw) * fwd + Math.cos(yaw) * side) * spd;
                vz = ( Math.cos(yaw) * fwd + Math.sin(yaw) * side) * spd;
            }

            if (mc.options.jumpKey.isPressed())  vy =  spd;
            if (mc.options.sneakKey.isPressed()) vy = -spd;

            mc.player.setVelocity(vx, vy, vz);
            mc.player.fallDistance = 0;

            if (mc.getNetworkHandler() != null) {
                mc.getNetworkHandler().sendPacket(new net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.PositionAndOnGround(
                        mc.player.getX(), mc.player.getY() + 0.0625, mc.player.getZ(), false));
            }

        } else if (mode.is("Glide")) {
            mc.player.fallDistance = 0;
            double vy = mc.player.getVelocity().y;
            if (vy < -0.03) {
                mc.player.setVelocity(mc.player.getVelocity().x, -0.03, mc.player.getVelocity().z);
            }
        }
    }
}
