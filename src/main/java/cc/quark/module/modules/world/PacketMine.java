package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPacketSend;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class PacketMine extends Module {

    private final BoolSetting log = register(new BoolSetting("Log", "Print mined block positions to chat", false));
    private final BoolSetting clientBreak = register(new BoolSetting("Client Break", "Also remove block client-side for instant visual feedback", true));
    private final BoolSetting swingHand = register(new BoolSetting("Swing Hand", "Swing hand when mining starts", true));

    public PacketMine() {
        super("PacketMine", "Instantly completes mining by sending a STOP_DESTROY_BLOCK after every START", Category.WORLD);
    }

    @EventHandler
    public void onPacketSend(EventPacketSend event) {
        if (mc.player == null || mc.world == null) return;
        if (!(event.getPacket() instanceof PlayerActionC2SPacket pkt)) return;
        if (pkt.getAction() != PlayerActionC2SPacket.Action.START_DESTROY_BLOCK) return;

        BlockPos pos = pkt.getPos();
        Direction face = pkt.getDirection();

        if (mc.world.getBlockState(pos).isAir()) return;

        mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(
                PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, pos, face));

        if (clientBreak.isEnabled()) {
            mc.world.breakBlock(pos, true, mc.player);
        }

        if (swingHand.isEnabled()) {
            mc.player.swingHand(net.minecraft.util.Hand.MAIN_HAND);
        }

        if (log.isEnabled()) {
            mc.player.sendMessage(Text.literal(
                    "[PacketMine] Mined " + mc.world.getBlockState(pos).getBlock().getName().getString()
                    + " at " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ()), false);
        }
    }
}
