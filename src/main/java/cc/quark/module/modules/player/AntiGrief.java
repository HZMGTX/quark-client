package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPacketSend;
import cc.quark.friend.FriendManager;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.util.ChatUtil;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.math.BlockPos;

import java.util.HashSet;
import java.util.Set;

public class AntiGrief extends Module {

    private final BoolSetting protectFriendBlocks = register(new BoolSetting(
            "Friend Blocks", "Block breaking near friend spawn positions (not yet implemented)", false));
    private final BoolSetting logCancelled = register(new BoolSetting(
            "Log", "Send chat message when a break is cancelled", false));

    private final Set<BlockPos> protectedPositions = new HashSet<>();

    public AntiGrief() {
        super("AntiGrief", "Cancels block-break packets for protected positions to prevent griefing", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        protectedPositions.clear();
    }

    public void addProtected(BlockPos pos) {
        protectedPositions.add(pos.toImmutable());
    }

    public void removeProtected(BlockPos pos) {
        protectedPositions.remove(pos);
    }

    @EventHandler
    public void onPacketSend(EventPacketSend event) {
        if (!(event.getPacket() instanceof PlayerActionC2SPacket pkt)) return;
        if (pkt.getAction() != PlayerActionC2SPacket.Action.START_DESTROY_BLOCK
                && pkt.getAction() != PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK) return;

        BlockPos pos = pkt.getPos();

        if (protectedPositions.contains(pos)) {
            event.cancel();
            if (logCancelled.isEnabled()) {
                ChatUtil.warn("[AntiGrief] Cancelled break at " + pos.getX() + "," + pos.getY() + "," + pos.getZ());
            }
        }
    }
}
