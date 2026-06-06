package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.ChatUtil;
import cc.quark.util.TimerUtil;
import net.minecraft.screen.MerchantScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.village.TradeOffer;

public class VillagerTrader extends Module {

    private final IntSetting tradeIndex = register(new IntSetting(
            "Trade Index", "Which trade slot to use (0 = first)", 0, 0, 20));
    private final IntSetting maxTrades = register(new IntSetting(
            "Max Trades", "Maximum number of trades per session", 20, 1, 64));
    private final BoolSetting repeatAll = register(new BoolSetting(
            "Repeat All", "Cycle through all available trades", false));
    private final BoolSetting closeWhenDone = register(new BoolSetting(
            "Close When Done", "Close the villager screen when trades are exhausted", true));
    private final TimerUtil timer = new TimerUtil();

    private int tradeCount = 0;
    private int currentIndex = 0;

    public VillagerTrader() {
        super("VillagerTrader", "Automates trading with villagers", Category.WORLD);
    }

    @Override
    public void onEnable() {
        tradeCount = 0;
        currentIndex = tradeIndex.get();
        timer.reset();
        ChatUtil.info("[VillagerTrader] Open a villager GUI to start trading.");
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.interactionManager == null) return;
        if (!(mc.player.currentScreenHandler instanceof MerchantScreenHandler merchant)) return;
        if (!timer.hasReached(300)) return;

        if (tradeCount >= maxTrades.get()) {
            ChatUtil.info("[VillagerTrader] Reached max trades (" + maxTrades.get() + ").");
            if (closeWhenDone.isEnabled()) mc.player.closeHandledScreen();
            disable();
            return;
        }

        // Pick the current trade index
        int idx = repeatAll.isEnabled() ? currentIndex : tradeIndex.get();
        var offers = merchant.getRecipes();
        if (offers.isEmpty()) return;
        if (idx >= offers.size()) idx = 0;

        TradeOffer offer = offers.get(idx);
        if (offer.isDisabled()) {
            if (repeatAll.isEnabled()) {
                currentIndex = (currentIndex + 1) % offers.size();
            } else {
                ChatUtil.warn("[VillagerTrader] Trade " + idx + " is locked out.");
                if (closeWhenDone.isEnabled()) mc.player.closeHandledScreen();
                disable();
            }
            return;
        }

        merchant.setRecipeIndex(idx);
        // Quick-move the result slot (slot 2 in merchant UI)
        mc.interactionManager.clickSlot(merchant.syncId, 2, 0, SlotActionType.QUICK_MOVE, mc.player);
        tradeCount++;

        if (repeatAll.isEnabled()) {
            currentIndex = (currentIndex + 1) % offers.size();
        }

        timer.reset();
    }
}
