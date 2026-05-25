package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPacketReceive;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.client.gui.screen.ingame.SignEditScreen;
import net.minecraft.network.packet.c2s.play.UpdateSignC2SPacket;
import net.minecraft.network.packet.s2c.play.SignEditorOpenS2CPacket;
import net.minecraft.util.math.BlockPos;

public class AutoSign extends Module {

    private final BoolSetting closeAfter = register(new BoolSetting(
            "Auto Close", "Close the sign editor automatically after filling", true));

    private static final String[] DEFAULT_LINES = {"Quark", "Client", "", ""};

    private BlockPos pendingPos = null;
    private boolean isFront     = true;

    public AutoSign() {
        super("AutoSign", "Automatically fills signs with preset text when the sign editor opens", Category.WORLD);
    }

    @EventHandler
    public void onPacketReceive(EventPacketReceive event) {
        if (event.getPacket() instanceof SignEditorOpenS2CPacket pkt) {
            pendingPos = pkt.getPos();
            isFront    = true;
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (!(mc.currentScreen instanceof SignEditScreen)) return;
        if (pendingPos == null || mc.getNetworkHandler() == null) return;

        mc.getNetworkHandler().sendPacket(new UpdateSignC2SPacket(
                pendingPos, isFront,
                DEFAULT_LINES[0], DEFAULT_LINES[1],
                DEFAULT_LINES[2], DEFAULT_LINES[3]));
        pendingPos = null;

        if (closeAfter.isEnabled()) mc.setScreen(null);
    }
}
