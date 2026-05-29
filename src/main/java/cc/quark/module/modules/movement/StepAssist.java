package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

/**
 * StepAssist - dynamically increases step height when a 1-block step is
 * detected directly ahead, then restores it afterward for a smooth stepping
 * experience without permanently raising step height.
 */
public class StepAssist extends Module {

    private static final float VANILLA_STEP = 0.6f;

    private final DoubleSetting stepHeight = register(new DoubleSetting(
            "Step Height", "Step height to use when stepping up (blocks)", 1.0, 0.6, 2.5));

    private boolean stepping = false;

    public StepAssist() {
        super("StepAssist", "Dynamically increase step height approaching a 1-block step", Category.MOVEMENT);
    }

    @Override
    public void onDisable() {
        if (mc.player == null) return;
        setStepHeight(VANILLA_STEP);
        stepping = false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        boolean stepAhead = detectStep();

        if (stepAhead && !stepping) {
            setStepHeight((float) stepHeight.get());
            stepping = true;
        } else if (!stepAhead && stepping) {
            setStepHeight(VANILLA_STEP);
            stepping = false;
        }
    }

    /**
     * Returns true when a 1-block-high solid block exists directly ahead
     * at foot level (the classic step-up scenario).
     */
    private boolean detectStep() {
        if (!mc.player.isOnGround()) return false;

        Vec3d vel = mc.player.getVelocity();
        double hLen = Math.sqrt(vel.x * vel.x + vel.z * vel.z);
        if (hLen < 0.01) return false;

        double nx = vel.x / hLen;
        double nz = vel.z / hLen;

        // Check 0.4 blocks ahead at foot level
        double checkX = mc.player.getX() + nx * 0.4;
        double checkZ = mc.player.getZ() + nz * 0.4;
        double checkY = mc.player.getY();

        BlockPos footPos   = new BlockPos((int) Math.floor(checkX),
                                          (int) Math.floor(checkY),
                                          (int) Math.floor(checkZ));
        BlockPos aboveFoot = footPos.up();

        boolean blockAtFoot   = !mc.world.getBlockState(footPos).isAir();
        boolean clearAboveFoot = mc.world.getBlockState(aboveFoot).isAir();

        return blockAtFoot && clearAboveFoot;
    }

    private void setStepHeight(float h) {
        EntityAttributeInstance attr =
                mc.player.getAttributeInstance(EntityAttributes.GENERIC_STEP_HEIGHT);
        if (attr != null) attr.setBaseValue(h);
    }
}
