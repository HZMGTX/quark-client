package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPacketSend;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.util.ChatUtil;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.math.BlockPos;

public class AntiGrief3 extends Module {

    private final BoolSetting alertMe = register(new BoolSetting(
            "AlertMe", "Send a chat message when a placement is cancelled", true));

    public AntiGrief3() {
        super("AntiGrief3", "Prevents placing fire, lava, or TNT near you by cancelling the packet", Category.PLAYER);
    }

    @EventHandler
    public void onPacketSend(EventPacketSend event) {
        if (!(event.getPacket() instanceof PlayerInteractBlockC2SPacket pkt)) return;
        if (mc.player == null) return;

        var held = mc.player.getInventory().getStack(mc.player.getInventory().selectedSlot);
        var item = held.getItem();

        boolean dangerous = item == Items.FLINT_AND_STEEL
                || item == Items.FIRE_CHARGE
                || item == Items.LAVA_BUCKET
                || item == Items.TNT;

        if (!dangerous) return;

        BlockPos pos = pkt.getBlockHitResult().getBlockPos();
        if (mc.player.squaredDistanceTo(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) > 64) return;

        event.cancel();
        if (alertMe.isEnabled()) {
            ChatUtil.warn("[AntiGrief3] Blocked dangerous placement of " + item.getName().getString());
        }
    }
}
