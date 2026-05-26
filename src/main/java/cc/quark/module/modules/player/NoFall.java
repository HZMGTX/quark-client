package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPacketSend;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.ModeSetting;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public class NoFall extends Module {

    private final ModeSetting mode = register(new ModeSetting(
            "Mode", "Method to prevent fall damage", "Packet", "Packet", "NoGround"));

    public NoFall() {
        super("NoFall", "Prevents fall damage by spoofing onGround", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (!mode.is("NoGround")) return;
        mc.player.fallDistance = 0;
    }

    @EventHandler
    public void onPacketSend(EventPacketSend event) {
        if (mc.player == null) return;
        if (!mode.is("Packet")) return;
        if (!(event.getPacket() instanceof PlayerMoveC2SPacket pkt)) return;
        if (mc.player.fallDistance <= 2) return;

        if (pkt instanceof PlayerMoveC2SPacket.Full f) {
            event.setPacket(new PlayerMoveC2SPacket.Full(
                    f.getX(0), f.getY(0), f.getZ(0), f.getYaw(0), f.getPitch(0), true));
        } else if (pkt instanceof PlayerMoveC2SPacket.PositionAndOnGround p) {
            event.setPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
                    p.getX(0), p.getY(0), p.getZ(0), true));
        } else if (pkt instanceof PlayerMoveC2SPacket.LookAndOnGround l) {
            event.setPacket(new PlayerMoveC2SPacket.LookAndOnGround(
                    l.getYaw(0), l.getPitch(0), true));
        } else {
            event.setPacket(new PlayerMoveC2SPacket.OnGroundOnly(true));
        }
    }
}
