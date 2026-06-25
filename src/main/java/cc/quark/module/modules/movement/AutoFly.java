package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.util.math.Vec3d;

/**
 * AutoFly - automatically toggles between walk mode and elytra glide mode.
 * When the player is airborne and has an elytra equipped, it activates
 * fall-flying. Holding Jump applies thrust. Landing auto-switches back to walk.
 */
public class AutoFly extends Module {

    private final DoubleSetting flySpeed = register(new DoubleSetting(
            "Fly Speed", "Horizontal speed while gliding", 0.8, 0.1, 3.0));

    private final DoubleSetting thrustPower = register(new DoubleSetting(
            "Thrust Power", "Upward boost per tick when Jump is held", 0.15, 0.05, 0.5));

    private final BoolSetting autoLaunch = register(new BoolSetting(
            "Auto Launch", "Start gliding automatically when falling", true));

    private final BoolSetting requireElytra = register(new BoolSetting(
            "Require Elytra", "Only activate when elytra is equipped", true));

    public AutoFly() {
        super("AutoFly", "Toggle between walk and elytra fly modes automatically", Category.MOVEMENT);
    }

    @Override
    public void onDisable() {
        // Stop gliding if still active
        if (mc.player != null && mc.player.isFallFlying() && mc.getNetworkHandler() != null) {
            mc.getNetworkHandler().sendPacket(
                    new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        boolean hasElytra = mc.player.getEquippedStack(EquipmentSlot.CHEST).getItem() == Items.ELYTRA;
        if (requireElytra.isEnabled() && !hasElytra) return;

        // Auto-launch: start gliding when falling fast enough
        if (autoLaunch.isEnabled()
                && !mc.player.isOnGround()
                && !mc.player.isFallFlying()
                && mc.player.getVelocity().y < -0.15) {
            launchGlide();
        }

        if (!mc.player.isFallFlying()) return;

        Vec3d vel = mc.player.getVelocity();
        float yaw = (float) Math.toRadians(mc.player.getYaw());

        double fwd = -Math.sin(yaw) * flySpeed.get();
        double side = Math.cos(yaw) * flySpeed.get();

        double newX = fwd * mc.player.input.movementForward
                + side * mc.player.input.movementSideways;
        double newZ = side * mc.player.input.movementForward
                - fwd * mc.player.input.movementSideways;

        // Vertical thrust
        double newY = vel.y;
        if (mc.options.jumpKey.isPressed()) {
            newY = Math.min(vel.y + thrustPower.get(), thrustPower.get() * 4);
        }

        if (mc.player.input.movementForward != 0 || mc.player.input.movementSideways != 0) {
            mc.player.setVelocity(newX, newY, newZ);
        } else {
            mc.player.setVelocity(vel.x * 0.9, newY, vel.z * 0.9);
        }
    }

    private void launchGlide() {
        if (mc.getNetworkHandler() == null) return;
        mc.getNetworkHandler().sendPacket(
                new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
    }
}
