package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

/**
 * IceSpeed - maintains a controlled movement speed on ice and packed/blue ice by
 * counteracting the excessive acceleration that vanilla ice normally provides.
 *
 * <p>By default ice has slipperiness 0.98 (vanilla blocks = 0.6), causing the player
 * to slide uncontrollably.  This module clamps the horizontal velocity each tick to a
 * configurable maximum so movement remains precise.
 */
public class IceSpeed extends Module {

    private final cc.quark.setting.ModeSetting mode = register(new cc.quark.setting.ModeSetting(
            "Mode", "Control mode on ice", "Boost", "Boost", "Cap", "Both"));

    private final DoubleSetting multiplier = register(new DoubleSetting(
            "Multiplier", "Speed multiplier when Boost mode is active", 2.5, 1.0, 8.0));

    private final DoubleSetting maxSpeed = register(new DoubleSetting(
            "Max Speed", "Maximum horizontal speed when Cap mode is active (blocks/tick)", 0.6, 0.1, 3.0));

    private final cc.quark.setting.BoolSetting allSurfaces = register(new cc.quark.setting.BoolSetting(
            "All Surfaces", "Apply to all ground surfaces, not just ice", false));

    public IceSpeed() {
        super("IceSpeed", "Maintains controlled speed on ice surfaces", Category.MOVEMENT);
    }

    @Override
    public String getSuffix() { return mode.get(); }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        if (!mc.player.isOnGround()) return;

        BlockPos belowPos = mc.player.getBlockPos().down();
        Block below = mc.world.getBlockState(belowPos).getBlock();
        boolean onIce = (below == Blocks.ICE || below == Blocks.PACKED_ICE
                      || below == Blocks.BLUE_ICE || below == Blocks.FROSTED_ICE);

        if (!onIce && !allSurfaces.isEnabled()) return;

        Vec3d vel = mc.player.getVelocity();
        double hSpeed = Math.sqrt(vel.x * vel.x + vel.z * vel.z);
        if (hSpeed < 0.01) return;

        String m = mode.get();
        if (m.equals("Boost") || m.equals("Both")) {
            mc.player.setVelocity(vel.x * multiplier.get(), vel.y, vel.z * multiplier.get());
        }
        if (m.equals("Cap") || m.equals("Both")) {
            Vec3d v2 = mc.player.getVelocity();
            double hs2 = Math.sqrt(v2.x * v2.x + v2.z * v2.z);
            if (hs2 > maxSpeed.get()) {
                double scale = maxSpeed.get() / hs2;
                mc.player.setVelocity(v2.x * scale, v2.y, v2.z * scale);
            }
        }
    }
}
