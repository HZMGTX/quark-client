package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class PortalFly extends Module {

    private final DoubleSetting speed = register(new DoubleSetting(
            "Speed", "Fly speed inside portals", 0.2, 0.01, 2.0));

    public PortalFly() {
        super("PortalFly", "Allows flying inside Nether/End portals", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        // Check if player is inside a portal block
        BlockPos pos = mc.player.getBlockPos();
        boolean inPortal = mc.world.getBlockState(pos).getBlock() == Blocks.NETHER_PORTAL ||
                           mc.world.getBlockState(pos).getBlock() == Blocks.END_GATEWAY;

        if (!inPortal) return;

        // Apply fly movement inside portal
        double s = speed.get();
        float yaw = mc.player.getYaw();
        float fwd = mc.player.input.movementForward;
        float side = mc.player.input.movementSideways;
        double yawRad = Math.toRadians(yaw);

        double motX = 0, motZ = 0, motY = 0;

        if (fwd != 0 || side != 0) {
            double len = Math.sqrt(fwd * fwd + side * side);
            motX = (-Math.sin(yawRad) * (fwd / len) + Math.cos(yawRad) * (side / len)) * s;
            motZ = (Math.cos(yawRad) * (fwd / len) + Math.sin(yawRad) * (side / len)) * s;
        }

        if (mc.options.jumpKey.isPressed()) {
            motY = s;
        } else if (mc.options.sneakKey.isPressed()) {
            motY = -s;
        }

        mc.player.setVelocity(motX, motY, motZ);
        // Prevent portal teleportation by resetting netherPortalTime
        mc.player.resetPortalCooldown();
    }
}
