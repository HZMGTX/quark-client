package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.ModeSetting;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

/**
 * AntiBounce - prevents the vertical velocity burst applied when the player
 * lands on slime blocks or beds.
 *
 * <ul>
 *   <li><b>Slime</b> - only cancel slime-block bounces.</li>
 *   <li><b>Bed</b>   - only cancel bed bounces.</li>
 *   <li><b>Both</b>  - cancel both.</li>
 * </ul>
 */
public class AntiBounce extends Module {

    private final ModeSetting mode = register(new ModeSetting(
            "Mode", "Which bounce types to cancel", "Both", "Both", "Slime", "Bed"));

    public AntiBounce() {
        super("AntiBounce", "Cancel slime and/or bed bounce", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        Vec3d vel = mc.player.getVelocity();
        // Only care when we're being launched upward by a surface
        if (vel.y <= 0) return;

        BlockPos below = mc.player.getBlockPos().down();
        boolean onSlime = mc.world.getBlockState(below).isOf(Blocks.SLIME_BLOCK);
        // Bed detection – check if the block below is any bed variant
        boolean onBed   = mc.world.getBlockState(below).getBlock().asItem().toString().contains("bed");

        boolean cancel = switch (mode.get()) {
            case "Slime" -> onSlime;
            case "Bed"   -> onBed;
            default      -> onSlime || onBed; // "Both"
        };

        if (cancel) {
            mc.player.setVelocity(vel.x, 0.0, vel.z);
        }
    }
}
