package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.util.ChatUtil;
import cc.quark.util.TimerUtil;
import net.minecraft.block.Blocks;
import net.minecraft.screen.FurnaceScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.BlockPos;

public class AutoSmelter2 extends Module {
    private final BoolSetting autoClose = register(new BoolSetting("Auto Close", "Close furnace when done", true));
    private final TimerUtil timer = new TimerUtil();

    public AutoSmelter2() { super("AutoSmelter2", "Enhanced auto-smelter with smart fuel management", Category.WORLD); }
    @Override public void onEnable() { mc.getEventBus().subscribe(this); }
    @Override public void onDisable() { mc.getEventBus().unsubscribe(this); }

    @EventHandler
    public void onTick(EventTick e) {
        if (mc.player == null || mc.interactionManager == null) return;
        if (!(mc.player.currentScreenHandler instanceof FurnaceScreenHandler furnace)) return;
        if (!timer.hasReached(200)) return;
        // Slot 0=input, 1=fuel, 2=output
        if (furnace.getSlot(2).hasStack()) {
            mc.interactionManager.clickSlot(furnace.syncId, 2, 0, SlotActionType.QUICK_MOVE, mc.player);
            timer.reset();
        } else if (!furnace.getSlot(1).hasStack()) {
            // Find fuel in inventory (coal, charcoal, wood)
            for (int i = 3; i < 39; i++) {
                var stack = furnace.getSlot(i).getStack();
                if (stack.getItem() == net.minecraft.item.Items.COAL || stack.getItem() == net.minecraft.item.Items.CHARCOAL) {
                    mc.interactionManager.clickSlot(furnace.syncId, i, 0, SlotActionType.QUICK_MOVE, mc.player);
                    timer.reset();
                    return;
                }
            }
        }
        if (autoClose.isEnabled() && !furnace.getSlot(0).hasStack() && !furnace.getSlot(2).hasStack()) {
            mc.player.closeHandledScreen();
        }
    }
}
