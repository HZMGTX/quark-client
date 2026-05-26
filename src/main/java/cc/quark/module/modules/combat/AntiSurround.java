package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.util.ChatUtil;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

/**
 * AntiSurround - detects obsidian placed around the player by an enemy surround.
 */
public class AntiSurround extends Module {

    private boolean warned;

    public AntiSurround() {
        super("AntiSurround", "Warns when surrounded by obsidian", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        warned = false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        BlockPos base = mc.player.getBlockPos();
        int count = 0;
        for (Direction dir : Direction.values()) {
            if (dir.getAxis().isVertical()) continue;
            BlockPos pos = base.offset(dir);
            if (mc.world.getBlockState(pos).isOf(Blocks.OBSIDIAN)) count++;
        }
        if (count >= 3 && !warned) {
            ChatUtil.warn("You are being surrounded!");
            warned = true;
        } else if (count < 3) {
            warned = false;
        }
    }
}
