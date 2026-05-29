package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.ModeSetting;
import net.minecraft.util.math.BlockPos;

/**
 * AutoSneak - automatically holds the sneak key.
 *
 * <ul>
 *   <li><b>Always</b>  - sneak at all times.</li>
 *   <li><b>Edge</b>    - sneak only when standing near the edge of a block (anti-fall).</li>
 *   <li><b>Moving</b>  - sneak only while moving.</li>
 *   <li><b>Still</b>   - sneak only while standing still.</li>
 * </ul>
 */
public class AutoSneak extends Module {

    private final ModeSetting mode = register(new ModeSetting(
            "Mode", "When to auto-sneak", "Always", "Always", "Edge", "Moving", "Still"));
    private final BoolSetting cancelSprint = register(new BoolSetting(
            "Cancel Sprint", "Stop sprinting while auto-sneaking", true));

    public AutoSneak() {
        super("AutoSneak", "Automatically hold sneak based on situation", Category.MOVEMENT);
    }

    @Override
    public void onDisable() {
        if (mc.player != null) mc.player.input.sneaking = false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        boolean moving = mc.player.input.movementForward != 0
                      || mc.player.input.movementSideways != 0;

        boolean shouldSneak = switch (mode.get()) {
            case "Always" -> true;
            case "Moving" -> moving;
            case "Still"  -> !moving;
            case "Edge"   -> isNearEdge();
            default       -> false;
        };

        mc.player.input.sneaking = shouldSneak;

        if (shouldSneak && cancelSprint.isEnabled()) {
            mc.player.setSprinting(false);
        }
    }

    /**
     * Returns true when the player is on the ground and at least one adjacent
     * horizontal block has no floor directly below it (i.e. there is a drop).
     */
    private boolean isNearEdge() {
        if (!mc.player.isOnGround()) return false;
        BlockPos standing = mc.player.getBlockPos();
        // Check the four cardinal neighbours one block out
        int[] dxs = {1, -1, 0,  0};
        int[] dzs = {0,  0, 1, -1};
        for (int i = 0; i < 4; i++) {
            BlockPos check = standing.add(dxs[i], 0, dzs[i]);
            // If that neighbour block is air and the block below it is also air it's a drop
            if (mc.world.getBlockState(check).isAir()
                    && mc.world.getBlockState(check.down()).isAir()) {
                return true;
            }
        }
        return false;
    }
}
