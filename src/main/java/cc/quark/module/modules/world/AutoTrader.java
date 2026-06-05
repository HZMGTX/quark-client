package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.util.ChatUtil;
import cc.quark.util.TimerUtil;
import net.minecraft.screen.MerchantScreenHandler;
import net.minecraft.screen.slot.SlotActionType;

public class AutoTrader extends Module {
    private final IntSetting tradeSlot = register(new IntSetting("Trade", "Trade slot to use (0-indexed)", 0, 0, 20));
    private final IntSetting amount = register(new IntSetting("Amount", "Times to trade", 10, 1, 64));
    private final TimerUtil timer = new TimerUtil();
    private int tradeCount = 0;

    public AutoTrader() { super("AutoTrader", "Auto-trades with villagers repeatedly", Category.WORLD); }
    @Override public void onEnable() { mc.getEventBus().subscribe(this); tradeCount = 0; }
    @Override public void onDisable() { mc.getEventBus().unsubscribe(this); }

    @EventHandler
    public void onTick(EventTick e) {
        if (mc.player == null || mc.interactionManager == null) return;
        if (!(mc.player.currentScreenHandler instanceof MerchantScreenHandler merchant)) return;
        if (!timer.hasReached(300)) return;
        if (tradeCount >= amount.get()) { mc.player.closeHandledScreen(); return; }
        merchant.setRecipeIndex(tradeSlot.get());
        mc.interactionManager.clickSlot(merchant.syncId, 2, 0, SlotActionType.QUICK_MOVE, mc.player);
        tradeCount++;
        timer.reset();
    }
}
