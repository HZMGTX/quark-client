package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.util.ChatUtil;
import net.minecraft.util.math.BlockPos;

import java.util.LinkedList;

public class PositionHistory extends Module {
    private final IntSetting maxHistory = register(new IntSetting("MaxHistory", "Positions to remember", 100, 10, 1000));
    private final LinkedList<BlockPos> history = new LinkedList<>();

    public PositionHistory() { super("PositionHistory", "Tracks position history for backtracking", Category.MISC); }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        BlockPos pos = mc.player.getBlockPos();
        if (history.isEmpty() || !history.getLast().equals(pos)) {
            if (history.size() >= maxHistory.getValue()) history.removeFirst();
            history.addLast(pos);
        }
    }

    @Override
    public void onDisable() {
        if (!history.isEmpty()) {
            BlockPos first = history.getFirst();
            ChatUtil.info("Start: " + first.getX() + "," + first.getY() + "," + first.getZ());
        }
        history.clear();
    }
}
