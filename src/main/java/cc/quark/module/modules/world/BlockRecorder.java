package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPacketSend;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.util.ChatUtil;
import cc.quark.util.TimerUtil;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class BlockRecorder extends Module {

    private final BoolSetting recording = register(new BoolSetting("Recording", "Record placed/broken blocks", false));
    private final BoolSetting replaying = register(new BoolSetting("Replaying", "Replay recorded blocks", false));

    private record BlockAction(boolean place, BlockPos pos, Direction face) {}

    private final List<BlockAction> recorded = new ArrayList<>();
    private final TimerUtil timer = new TimerUtil();
    private int replayIndex = 0;

    public BlockRecorder() {
        super("BlockRecorder", "Records placed/broken blocks and replays them on demand", Category.WORLD);
    }

    @Override
    public void onEnable() {
        replayIndex = 0;
        timer.reset();
        ChatUtil.info("BlockRecorder: " + recorded.size() + " actions recorded.");
    }

    @Override
    public void onDisable() {
        replaying.setValue(Boolean.FALSE);
        recording.setValue(Boolean.FALSE);
    }

    @EventHandler
    public void onPacketSend(EventPacketSend event) {
        if (!recording.isEnabled()) return;
        if (mc.player == null) return;

        if (event.getPacket() instanceof PlayerActionC2SPacket pkt) {
            if (pkt.getAction() == PlayerActionC2SPacket.Action.START_DESTROY_BLOCK) {
                recorded.add(new BlockAction(false, pkt.getPos(), pkt.getDirection()));
            }
        } else if (event.getPacket() instanceof PlayerInteractBlockC2SPacket pkt) {
            BlockHitResult hit = pkt.getBlockHitResult();
            recorded.add(new BlockAction(true, hit.getBlockPos(), hit.getSide()));
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (!replaying.isEnabled()) return;
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(100)) return;
        if (replayIndex >= recorded.size()) {
            replaying.setValue(Boolean.FALSE);
            replayIndex = 0;
            ChatUtil.success("BlockRecorder: Replay finished.");
            return;
        }

        BlockAction action = recorded.get(replayIndex);
        if (action.place()) {
            Vec3d hitVec = Vec3d.ofCenter(action.pos());
            BlockHitResult hit = new BlockHitResult(hitVec, action.face(), action.pos(), false);
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
        } else {
            mc.interactionManager.attackBlock(action.pos(), action.face());
            mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(
                    PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, action.pos(), action.face()));
        }
        mc.player.swingHand(Hand.MAIN_HAND);
        replayIndex++;
        timer.reset();
    }
}
