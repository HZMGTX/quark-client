package cc.quark.module.modules.staff;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.ChatUtil;
import cc.quark.util.TimerUtil;

public class ServerCrasher extends Module {
    private final IntSetting packetRate = register(new IntSetting("Packets/s", "Packets per second", 20, 1, 100));
    private final BoolSetting warnFirst = register(new BoolSetting("Warn First", "Warn before activating", true));
    private final TimerUtil timer = new TimerUtil();
    private boolean warned = false;

    public ServerCrasher() { super("ServerCrasher", "Staff tool: detects lag-attack vectors via packet flooding", Category.STAFF); }
    @Override public void onEnable() { warned = false; ChatUtil.warn("ServerCrasher enabled - for authorized testing only!"); }

    @EventHandler
    public void onTick(EventTick e) {
        if (mc.player == null || warnFirst.isEnabled() && !warned) { warned = true; ChatUtil.warn("Activate again to confirm stress test."); disable(); return; }
        if (!timer.hasReached(1000 / packetRate.get())) return;
        // Stress test via movement packets
        mc.player.networkHandler.sendPacket(new net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.OnGroundOnly(mc.player.isOnGround(), false));
        timer.reset();
    }
}
