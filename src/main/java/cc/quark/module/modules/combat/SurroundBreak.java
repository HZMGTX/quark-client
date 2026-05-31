package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.util.EntityUtil;
import cc.quark.util.TimerUtil;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.Comparator;
import java.util.List;

public class SurroundBreak extends Module {

    private final IntSetting range = register(new IntSetting("Range", "Range to find surrounded players", 5, 1, 8));
    private final TimerUtil timer = new TimerUtil();

    public SurroundBreak() {
        super("SurroundBreak", "Auto-mines obsidian blocks surrounding an enemy player", Category.COMBAT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(200)) return;

        List<PlayerEntity> players = EntityUtil.getEntitiesOfType(PlayerEntity.class, range.get());
        players.removeIf(p -> p == mc.player || EntityUtil.isFriend(p));
        players.sort(Comparator.comparingDouble(EntityUtil::distanceTo));
        if (players.isEmpty()) return;

        PlayerEntity target = players.get(0);
        BlockPos targetPos = target.getBlockPos();

        for (Direction dir : Direction.values()) {
            BlockPos neighbor = targetPos.offset(dir);
            BlockState state = mc.world.getBlockState(neighbor);
            if (state.getBlock() == Blocks.OBSIDIAN || state.getBlock() == Blocks.CRYING_OBSIDIAN) {
                if (mc.player.squaredDistanceTo(neighbor.getX(), neighbor.getY(), neighbor.getZ()) <= range.get() * range.get()) {
                    mc.interactionManager.attackBlock(neighbor, dir.getOpposite());
                    timer.reset();
                    return;
                }
            }
        }
    }
}
