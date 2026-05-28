package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.block.Blocks;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class InstaBreak extends Module {

    private final IntSetting delay = register(new IntSetting("Delay", "Delay between breaks (ticks)", 0, 0, 10));
    private final BoolSetting onlyWeak = register(new BoolSetting("Only Weak", "Only instabreak blocks with hardness < 1.5", false));
    private final BoolSetting swingHand = register(new BoolSetting("Swing Hand", "Swing hand animation when breaking", true));

    private final TimerUtil timer = new TimerUtil();

    public InstaBreak() {
        super("InstaBreak", "Instantly breaks blocks by sending start and stop packets in the same tick", Category.WORLD);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(delay.get() * 50L)) return;

        HitResult hit = mc.crosshairTarget;
        if (hit == null || hit.getType() != HitResult.Type.BLOCK) return;

        BlockHitResult blockHit = (BlockHitResult) hit;
        BlockPos pos = blockHit.getBlockPos();
        Direction face = blockHit.getSide();

        if (mc.world.getBlockState(pos).isAir()) return;
        if (mc.world.getBlockState(pos).getBlock() == Blocks.BEDROCK) return;

        float hardness = mc.world.getBlockState(pos).getHardness(mc.world, pos);
        if (hardness < 0) return;
        if (onlyWeak.isEnabled() && hardness >= 1.5f) return;

        mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(
                PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, pos, face));
        mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(
                PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, pos, face));

        mc.world.breakBlock(pos, true, mc.player);

        if (swingHand.isEnabled()) {
            mc.player.swingHand(Hand.MAIN_HAND);
        }

        timer.reset();
    }
}
