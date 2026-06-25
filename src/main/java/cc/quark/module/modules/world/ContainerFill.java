package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.ModeSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.*;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.screen.slot.SlotActionType;

public class ContainerFill extends Module {

    private final ModeSetting fillItem = register(new ModeSetting("FillItem", "Item to fill container with", "Cobblestone",
            "Cobblestone", "Dirt", "Sand", "Gravel", "Stone", "Oak Planks", "Torch"));

    private final TimerUtil timer = new TimerUtil();

    public ContainerFill() {
        super("ContainerFill", "Fills selected container with specified item from inventory", Category.WORLD);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!(mc.currentScreen instanceof HandledScreen<?>)) return;
        if (!timer.hasReached(80)) return;

        var handler = mc.player.currentScreenHandler;
        int containerSize = handler.slots.size() - 36;
        if (containerSize <= 0) return;

        Item targetItem = getTargetItem();
        if (targetItem == null) return;

        for (int i = containerSize; i < handler.slots.size(); i++) {
            var slot = handler.slots.get(i);
            if (!slot.hasStack() || slot.getStack().getItem() != targetItem) continue;

            for (int j = 0; j < containerSize; j++) {
                if (handler.slots.get(j).hasStack()) continue;
                mc.interactionManager.clickSlot(handler.syncId, i, 0, SlotActionType.QUICK_MOVE, mc.player);
                timer.reset();
                return;
            }
        }

        mc.player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(handler.syncId));
        mc.currentScreen.close();
    }

    private Item getTargetItem() {
        return switch (fillItem.get()) {
            case "Cobblestone" -> Items.COBBLESTONE;
            case "Dirt" -> Items.DIRT;
            case "Sand" -> Items.SAND;
            case "Gravel" -> Items.GRAVEL;
            case "Stone" -> Items.STONE;
            case "Oak Planks" -> Items.OAK_PLANKS;
            case "Torch" -> Items.TORCH;
            default -> null;
        };
    }
}
