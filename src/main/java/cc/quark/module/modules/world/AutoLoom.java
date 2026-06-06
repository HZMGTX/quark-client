package cc.quark.module.modules.world;

import cc.quark.Quark;
import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.module.setting.IntSetting;
import net.minecraft.client.gui.screen.ingame.LoomScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.LoomScreenHandler;
import net.minecraft.screen.slot.SlotActionType;

public class AutoLoom extends Module {

    private final IntSetting patternSlot = new IntSetting("Pattern", 0, 0, 15);
    private final IntSetting delay = new IntSetting("Delay", 3, 1, 10);

    private int timer = 0;

    public AutoLoom() {
        super("AutoLoom", "Automatically applies banner patterns in a loom", Category.WORLD);
        addSettings(patternSlot, delay);
    }

    @Override public void onEnable()  { Quark.mc.getEventBus().subscribe(this); }
    @Override public void onDisable() { Quark.mc.getEventBus().unsubscribe(this); }

    @EventHandler
    public void onTick(EventTick event) {
        var mc = Quark.mc;
        if (mc == null || mc.player == null) return;
        if (!(mc.currentScreen instanceof LoomScreen)) return;
        if (!(mc.player.currentScreenHandler instanceof LoomScreenHandler handler)) return;

        if (++timer < delay.get()) return;
        timer = 0;

        ItemStack result = handler.getOutputSlot().getStack();
        if (result.isEmpty()) return;

        mc.interactionManager.clickSlot(handler.syncId,
            handler.getOutputSlot().id, 0, SlotActionType.QUICK_MOVE, mc.player);
    }
}
