package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class HoverStep extends Module {

    private final DoubleSetting maxStep = register(new DoubleSetting(
            "Max Step", "Maximum block height to step up smoothly", 1.5, 0.5, 3.0));

    private double targetY = Double.MIN_VALUE;
    private boolean stepping = false;

    public HoverStep() {
        super("HoverStep", "Step up blocks without jumping animation", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        stepping = false;
        targetY = Double.MIN_VALUE;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        Vec3d vel = mc.player.getVelocity();
        float fwd = mc.player.input.movementForward;
        float side = mc.player.input.movementSideways;
        boolean moving = (fwd != 0 || side != 0);

        if (stepping) {
            double dy = targetY - mc.player.getY();
            if (dy <= 0.05 || mc.player.isOnGround()) {
                stepping = false;
                mc.player.setVelocity(vel.x, 0, vel.z);
            } else {
                double stepSpeed = Math.min(dy, 0.2);
                mc.player.setVelocity(vel.x, stepSpeed, vel.z);
            }
            return;
        }

        // Detect blocked horizontal movement → attempt step
        if (!moving || !mc.player.isOnGround()) return;

        float yaw = mc.player.getYaw();
        double yawRad = Math.toRadians(yaw);
        double len = Math.sqrt(fwd * fwd + side * side);
        double dx = (-Math.sin(yawRad) * (fwd / len) + Math.cos(yawRad) * (side / len));
        double dz = (Math.cos(yawRad) * (fwd / len) + Math.sin(yawRad) * (side / len));

        Vec3d pos = mc.player.getPos();

        // Check blocks ahead at ground level
        for (double h = 0.5; h <= maxStep.get(); h += 0.5) {
            BlockPos checkPos = BlockPos.ofFloored(pos.add(dx * 0.5, h - 0.1, dz * 0.5));
            if (mc.world.getBlockState(checkPos).isAir()) {
                // Gap found at height h; step up to it
                BlockPos groundPos = checkPos.down();
                if (!mc.world.getBlockState(groundPos).isAir()) {
                    targetY = groundPos.getY() + 1.0;
                    stepping = true;
                    break;
                }
            }
        }
    }
}
