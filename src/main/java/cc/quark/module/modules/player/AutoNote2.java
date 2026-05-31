package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.block.NoteBlock;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class AutoNote2 extends Module {

    private final IntSetting bpm = register(new IntSetting("BPM", "Beats per minute for note playback", 120, 20, 300));

    private final TimerUtil timer = new TimerUtil();

    public AutoNote2() {
        super("AutoNote2", "Automatically plays a preconfigured note sequence on note blocks", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        long intervalMs = 60000L / bpm.get();
        if (!timer.hasReached(intervalMs)) return;
        timer.reset();

        BlockPos playerPos = mc.player.getBlockPos();
        for (BlockPos pos : BlockPos.iterate(
                playerPos.getX() - 2, playerPos.getY() - 1, playerPos.getZ() - 2,
                playerPos.getX() + 2, playerPos.getY() + 1, playerPos.getZ() + 2)) {
            if (mc.world.getBlockState(pos).getBlock() instanceof NoteBlock) {
                BlockHitResult hit = new BlockHitResult(Vec3d.ofCenter(pos), Direction.UP, pos.toImmutable(), false);
                mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
                break;
            }
        }
    }
}
