package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPacketSend;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.ModeSetting;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;

public class OpenInventory extends Module {

    private final ModeSetting mode = register(new ModeSetting(
            "Mode", "Inventory open behavior",
            "Keep Open", "Keep Open", "Auto Open"));
    private final BoolSetting cancelClose = register(new BoolSetting(
            "Cancel Close", "Block inventory-close packets from being sent", true));

    public OpenInventory() {
        super("OpenInventory", "Keeps inventory open while moving or riding entities", Category.PLAYER);
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

        if (cancelClose.isEnabled() && event.getPacket() instanceof CloseHandledScreenC2SPacket) {
            event.cancel();
        }
    }
}
