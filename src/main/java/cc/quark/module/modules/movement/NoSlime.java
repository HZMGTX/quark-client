package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

/**
 * NoSlime - when landing on or standing on a slime block: zero the upward Y
 * velocity bounce so the player doesn't get launched, and optionally cancel
 * the horizontal slowdown.
 */
public class NoSlime extends Module {

    private final BoolSetting noBounce = register(new BoolSetting(
            "No Bounce", "Cancel vertical bounce from slime", true));
    private final BoolSetting noSlow = register(new BoolSetting(
            "No Slow", "Cancel horizontal slowdown on slime", true));

    public NoSlime() {
        super("NoSlime", "Cancel slime block bounce and horizontal slowdown", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        BlockPos below = mc.player.getBlockPos().down();
        if (!mc.world.getBlockState(below).isOf(Blocks.SLIME_BLOCK)) return;

        Vec3d v = mc.player.getVelocity();

        if (noBounce.isEnabled() && v.y > 0 && mc.player.isOnGround()) {
            mc.player.setVelocity(v.x, 0.0, v.z);
        }

        if (noSlow.isEnabled()) {
            // Slime slows horizontal momentum; re-apply expected friction factor
            double hLen = Math.sqrt(v.x * v.x + v.z * v.z);
            if (hLen > 0.001) {
                // Vanilla slime applies ~0.4 friction; compensate
                mc.player.setVelocity(v.x * 2.5, mc.player.getVelocity().y, v.z * 2.5);
            }
        }
    }
}
