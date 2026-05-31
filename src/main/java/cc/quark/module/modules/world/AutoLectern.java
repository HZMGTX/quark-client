package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LecternBlock;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class AutoLectern extends Module {

    private final BoolSetting allBooks = register(new BoolSetting("AllBooks", "Retrieve books from all nearby lecterns, not just targeted", true));

    private final TimerUtil timer = new TimerUtil();

    public AutoLectern() {
        super("AutoLectern", "Retrieves books from lecterns automatically", Category.WORLD);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(400)) return;

        if (allBooks.isEnabled()) {
            BlockPos center = mc.player.getBlockPos();
            for (BlockPos pos : BlockPos.iterate(center.add(-4, -2, -4), center.add(4, 2, 4))) {
                BlockState state = mc.world.getBlockState(pos);
                if (!state.isOf(Blocks.LECTERN)) continue;
                if (!state.get(LecternBlock.HAS_BOOK)) continue;
                takeBook(pos.toImmutable());
                return;
            }
        } else {
            var hit = mc.crosshairTarget;
            if (hit instanceof BlockHitResult bhr) {
                BlockPos pos = bhr.getBlockPos();
                BlockState state = mc.world.getBlockState(pos);
                if (state.isOf(Blocks.LECTERN) && state.get(LecternBlock.HAS_BOOK)) {
                    takeBook(pos);
                }
            }
        }
    }

    private void takeBook(BlockPos pos) {
        mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(
                PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, pos, Direction.UP));
        timer.reset();
    }
}
