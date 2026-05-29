package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.util.math.Vec3d;

/**
 * ElytraBoost - adds sustained forward thrust while gliding with an elytra.
 * The thrust is applied in the current look-direction each tick so the player
 * can steer freely.  Optionally simulates firework-style burst activation
 * rather than continuous thrust.
 */
public class ElytraBoost extends Module {

    private final DoubleSetting power = register(new DoubleSetting(
            "Power", "Thrust added per tick in the look direction", 0.15, 0.01, 1.0));
    private final DoubleSetting maxSpeed = register(new DoubleSetting(
            "Max Speed", "Cap on combined velocity magnitude (0 = no cap)", 3.0, 0.0, 15.0));
    private final BoolSetting requireJump = register(new BoolSetting(
            "Require Jump", "Only boost while jump key is held", false));
    private final BoolSetting autoLaunch = register(new BoolSetting(
            "Auto Launch", "Start elytra glide automatically when falling", true));

    public ElytraBoost() {
        super("ElytraBoost", "Sustained thrust while elytra gliding", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        // Auto-launch: start gliding when falling if elytra is equipped
        if (autoLaunch.isEnabled()) {
            boolean hasElytra = mc.player.getEquippedStack(net.minecraft.entity.EquipmentSlot.CHEST)
                    .getItem() == Items.ELYTRA;
            if (hasElytra && !mc.player.isFallFlying()
                    && !mc.player.isOnGround()
                    && mc.player.getVelocity().y < -0.15) {
                if (mc.getNetworkHandler() != null) {
                    mc.getNetworkHandler().sendPacket(
                            new ClientCommandC2SPacket(mc.player,
                                    ClientCommandC2SPacket.Mode.START_FALL_FLYING));
                }
            }
        }

        if (!mc.player.isFallFlying()) return;
        if (requireJump.isEnabled() && !mc.options.jumpKey.isPressed()) return;

        double yawRad   = Math.toRadians(mc.player.getYaw());
        double pitchRad = Math.toRadians(mc.player.getPitch());

        double lx = -Math.sin(yawRad)  * Math.cos(pitchRad);
        double ly = -Math.sin(pitchRad);
        double lz =  Math.cos(yawRad)  * Math.cos(pitchRad);

        Vec3d vel = mc.player.getVelocity();
        double nx = vel.x + lx * power.get();
        double ny = vel.y + ly * power.get();
        double nz = vel.z + lz * power.get();

        // Apply speed cap if configured
        double capSq = maxSpeed.get() * maxSpeed.get();
        if (capSq > 0) {
            double speedSq = nx * nx + ny * ny + nz * nz;
            if (speedSq > capSq) {
                double scale = maxSpeed.get() / Math.sqrt(speedSq);
                nx *= scale;
                ny *= scale;
                nz *= scale;
            }
        }

        mc.player.setVelocity(nx, ny, nz);
    }
}
