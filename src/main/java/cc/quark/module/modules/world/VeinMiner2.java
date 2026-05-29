package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPacketSend;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.block.Block;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

public class VeinMiner2 extends Module {

    private final IntSetting maxBlocks = register(new IntSetting(
            "Max Blocks", "Maximum connected blocks to break", 32, 1, 64));
    private final BoolSetting requireSneaking = register(new BoolSetting(
            "Require Sneaking", "Only activate when sneaking", false));

    public VeinMiner2() {
        super("VeinMiner2", "Mines all connected blocks of the same type when breaking one (packet-based)", Category.WORLD);
    }

    @EventHandler
    public void onPacketSend(EventPacketSend event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (requireSneaking.isEnabled() && !mc.player.isSneaking()) return;
        if (!(event.getPacket() instanceof PlayerActionC2SPacket pkt)) return;
        if (pkt.getAction() != PlayerActionC2SPacket.Action.START_DESTROY_BLOCK) return;

        BlockPos origin = pkt.getPos();
        var originState = mc.world.getBlockState(origin);
        if (originState.isAir()) return;
        Block target = originState.getBlock();

        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> queue = new ArrayDeque<>();
        queue.add(origin);
        visited.add(origin);
        int count = 0;

        while (!queue.isEmpty() && count < maxBlocks.get()) {
            BlockPos pos = queue.poll();
            if (!pos.equals(origin)) {
                mc.interactionManager.attackBlock(pos, Direction.UP);
                mc.player.swingHand(Hand.MAIN_HAND);
                mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(
                        PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, pos, Direction.UP));
            }
            count++;
            for (Direction dir : Direction.values()) {
                BlockPos neighbor = pos.offset(dir);
                if (visited.contains(neighbor)) continue;
                if (!mc.world.getBlockState(neighbor).getBlock().equals(target)) continue;
                visited.add(neighbor);
                queue.add(neighbor);
            }
        }
    }
}
