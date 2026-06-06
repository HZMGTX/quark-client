package cc.quark.module.modules.world;

import cc.quark.Quark;
import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.module.setting.BoolSetting;
import cc.quark.module.setting.IntSetting;
import net.minecraft.client.gui.screen.ingame.SmithingScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.SmithingScreenHandler;
import net.minecraft.screen.slot.SlotActionType;

public class AutoSmith extends Module {

    private final BoolSetting autoClose = new BoolSetting("AutoClose", true);
    private final IntSetting delay = new IntSetting("Delay", 2, 1, 10);

    private int timer = 0;

    public AutoSmith() {
        super("AutoSmith", "Automatically upgrades gear in a smithing table", Category.WORLD);
        addSettings(autoClose, delay);
    }

    @Override public void onEnable()  { Quark.mc.getEventBus().subscribe(this); }
    @Override public void onDisable() { Quark.mc.getEventBus().unsubscribe(this); }

    @EventHandler
    public void onTick(EventTick event) {
        var mc = Quark.mc;
        if (mc == null || mc.player == null) return;
        if (!(mc.currentScreen instanceof SmithingScreen)) return;
        if (!(mc.player.currentScreenHandler instanceof SmithingScreenHandler handler)) return;

        if (++timer < delay.get()) return;
        timer = 0;

        // Slot 0 = template, 1 = base, 2 = addition, 3 = result
        ItemStack result = handler.slots.get(3).getStack();
        if (result.isEmpty()) return;

        // Take the result
        mc.interactionManager.clickSlot(handler.syncId, 3, 0, SlotActionType.QUICK_MOVE, mc.player);

        if (autoClose.isEnabled() && handler.slots.get(1).getStack().isEmpty()) {
            mc.player.closeHandledScreen();
        }
    }
}
