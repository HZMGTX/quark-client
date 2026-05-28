package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPacketSend;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.ModeSetting;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;

public class OpenInventory extends Module {

    private final ModeSetting mode = register(new ModeSetting("Mode", "Inventory bypass mode", "Keep Open", "Keep Open", "Auto Open"));

    public OpenInventory() {
        super("OpenInventory", "Keeps server-side inventory open while moving", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (mode.is("Auto Open") && mc.currentScreen == null) {
            mc.setScreen(new InventoryScreen(mc.player));
        }
    }

    @EventHandler
    public void onPacketSend(EventPacketSend event) {
        if (mc.player == null) return;
        if (event.getPacket() instanceof CloseHandledScreenC2SPacket) {
            event.cancel();
        }
    }
}
