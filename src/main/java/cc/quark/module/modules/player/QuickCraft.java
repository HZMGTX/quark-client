package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.client.gui.screen.ingame.CraftingScreen;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.slot.SlotActionType;

public class QuickCraft extends Module {

    private final BoolSetting autoOpen = register(new BoolSetting(
            "AutoOpen", "Automatically open nearest crafting table", false));

    private final TimerUtil timer = new TimerUtil();

    public QuickCraft() {
        super("QuickCraft", "Shift-clicks to instantly craft full stacks on a workbench", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        if (!timer.hasReached(100)) return;
        timer.reset();

        if (!(mc.currentScreen instanceof CraftingScreen)) return;
        if (!(mc.player.currentScreenHandler instanceof CraftingScreenHandler handler)) return;

        var output = handler.getSlot(0).getStack();
        if (output.isEmpty()) return;

        while (!handler.getSlot(0).getStack().isEmpty()) {
            mc.interactionManager.clickSlot(handler.syncId, 0, 0, SlotActionType.QUICK_MOVE, mc.player);
        }
    }
}
