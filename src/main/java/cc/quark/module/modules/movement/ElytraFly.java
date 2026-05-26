package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.EnumSetting;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.util.math.Vec3d;

public class ElytraFly extends Module {

    public enum ElytraMode {
        BOOST, VANILLA, PITCH
    }

    private final EnumSetting<ElytraMode> mode = register(new EnumSetting<>(
            "Mode", "Elytra flight mode", ElytraMode.BOOST));

    private final DoubleSetting speed = register(new DoubleSetting(
            "Speed", "Fly speed", 5.0, 1.0, 15.0));

    private final BoolSetting autoLaunch = register(new BoolSetting(
            "Auto Launch", "Automatically start gliding when falling", true));

    public ElytraFly() {
        super("ElytraFly", "Fly using elytra mechanics", Category.MOVEMENT, 0);
    }

    @Override
    public void onDisable() {
        if (mc.player != null) {
            mc.player.getAbilities().allowFlying = false;
            mc.player.getAbilities().flying = false;
        }
    }

    @Override
    public String getSuffix() {
        return mode.get().name();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        boolean hasElytra = mc.player.getEquippedStack(net.minecraft.entity.EquipmentSlot.CHEST)
                .getItem() == Items.ELYTRA;

        if (autoLaunch.isEnabled() && hasElytra && !mc.player.isFallFlying()
                && !mc.player.isOnGround() && mc.player.getVelocity().y < -0.1) {
            if (mc.getNetworkHandler() != null) {
                mc.getNetworkHandler().sendPacket(
                        new ClientCommandC2SPacket(
                                mc.player,
                                ClientCommandC2SPacket.Mode.START_FALL_FLYING));
            }
        }

        if (!mc.player.isFallFlying()) return;

        double spd = speed.get() * 0.05;
        float fwd = mc.player.input.movementForward;
        float side = mc.player.input.movementSideways;
        double yawRad = Math.toRadians(mc.player.getYaw());

        switch (mode.get()) {
            case BOOST -> {
                Vec3d look = mc.player.getRotationVector();
                Vec3d vel = mc.player.getVelocity();
                mc.player.setVelocity(
                        vel.x + look.x * spd,
                        vel.y + look.y * spd,
                        vel.z + look.z * spd);
            }

            case VANILLA -> {
                double dirX = (-Math.sin(yawRad) * fwd + Math.cos(yawRad) * side) * spd * 2.0;
                double dirZ = (Math.cos(yawRad) * fwd + Math.sin(yawRad) * side) * spd * 2.0;
                double y = mc.player.getVelocity().y;
                if (mc.options.jumpKey.isPressed()) y += spd;
                if (mc.options.sneakKey.isPressed()) y -= spd;
                mc.player.setVelocity(dirX, y, dirZ);
                mc.player.fallDistance = 0;
            }

            case PITCH -> {
                double dirX = (-Math.sin(yawRad) * fwd + Math.cos(yawRad) * side) * spd * 2.0;
                double dirZ = (Math.cos(yawRad) * fwd + Math.sin(yawRad) * side) * spd * 2.0;
                double pitchRad = Math.toRadians(mc.player.getPitch());
                double y = -Math.sin(pitchRad) * spd * 2.0;
                if (mc.options.jumpKey.isPressed()) y += spd;
                if (mc.options.sneakKey.isPressed()) y -= spd;
                mc.player.setVelocity(dirX, y, dirZ);
                mc.player.fallDistance = 0;
            }
        }
    }

    public boolean isFakeElytra() {
        return false;
    }
}
