package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.screen.slot.SlotActionType;

public class ItemMover extends Module {

    private final BoolSetting loop = register(new BoolSetting("Loop", "Continuously move items back and forth", false));
    private final IntSetting delay = register(new IntSetting("Delay", "Delay between item moves (ms)", 150, 50, 1000));

    private final TimerUtil timer = new TimerUtil();
    private boolean movingToContainer = true;

    public ItemMover() {
        super("ItemMover", "Moves items between two inventories", Category.WORLD);
    }

    @Override
    public void onEnable() {
        timer.reset();
        movingToContainer = true;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!(mc.currentScreen instanceof HandledScreen<?>)) return;
        if (!timer.hasReached(delay.get())) return;

        var handler = mc.player.currentScreenHandler;
        int containerSize = handler.slots.size() - 36;
        if (containerSize <= 0) return;

        if (movingToContainer) {
            for (int i = containerSize; i < handler.slots.size(); i++) {
                if (!handler.slots.get(i).hasStack()) continue;
                mc.interactionManager.clickSlot(handler.syncId, i, 0, SlotActionType.QUICK_MOVE, mc.player);
                timer.reset();
                return;
            }
            if (loop.isEnabled()) {
                movingToContainer = false;
            } else {
                mc.player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(handler.syncId));
                mc.currentScreen.close();
            }
        } else {
            for (int i = 0; i < containerSize; i++) {
                if (!handler.slots.get(i).hasStack()) continue;
                mc.interactionManager.clickSlot(handler.syncId, i, 0, SlotActionType.QUICK_MOVE, mc.player);
                timer.reset();
                return;
            }
            movingToContainer = true;
        }
    }
}
