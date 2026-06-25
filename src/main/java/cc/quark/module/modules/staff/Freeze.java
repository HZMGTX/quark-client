package cc.quark.module.modules.staff;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPacketSend;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public class Freeze extends Module {

    private final BoolSetting cancelPackets = register(new BoolSetting(
            "Cancel Packets", "Cancel outgoing movement packets while frozen", true));

    public Freeze() {
        super("Freeze", "Freeze a target player in place for inspection.", Category.STAFF, 0);
    }

    @Override
    public void onEnable() {
        if (mc.player == null) return;
        // Zero out velocity so the player stops moving immediately
        mc.player.setVelocity(0, 0, 0);
    }

    @Override
    public void onDisable() {
        // Nothing extra needed; movement resumes naturally when packets flow again
    }

    @EventHandler
    public void onPacketSend(EventPacketSend event) {
        if (!cancelPackets.isEnabled()) return;
        if (mc.player == null) return;

        // Cancel all movement packets so the server keeps the player frozen
        if (event.getPacket() instanceof PlayerMoveC2SPacket) {
            event.cancel();
        }
    }
}
