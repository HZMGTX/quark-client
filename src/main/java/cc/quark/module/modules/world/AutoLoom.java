package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.client.gui.screen.ingame.LoomScreen;
import net.minecraft.item.BannerItem;
import net.minecraft.item.DyeItem;
import net.minecraft.item.Items;
import net.minecraft.screen.LoomScreenHandler;
import net.minecraft.screen.slot.SlotActionType;

public class AutoLoom extends Module {

    private final IntSetting delay = register(new IntSetting(
            "Delay", "Milliseconds between actions", 200, 50, 1000));

    private final TimerUtil timer = new TimerUtil();

    public AutoLoom() {
        super("AutoLoom", "Automatically operates the loom to apply banner patterns", Category.WORLD);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.interactionManager == null) return;
        if (!(mc.currentScreen instanceof LoomScreen)) return;
        if (!timer.hasReached(delay.get())) return;

        var handler = mc.player.currentScreenHandler;
        if (!(handler instanceof LoomScreenHandler loomHandler)) return;

        // Loom slots:
        // 0: banner input
        // 1: dye input
        // 2: pattern input
        // 3: result output

        int containerSize = loomHandler.slots.size();
        int invStart = 4; // player inventory starts at slot 4

        // Take result if available
        if (loomHandler.slots.get(3).hasStack()) {
            mc.interactionManager.clickSlot(handler.syncId, 3, 0, SlotActionType.QUICK_MOVE, mc.player);
            timer.reset();
            return;
        }

        // Fill banner slot 0
        if (!loomHandler.slots.get(0).hasStack()) {
            for (int i = invStart; i < containerSize; i++) {
                var slot = loomHandler.slots.get(i);
                if (slot.hasStack() && slot.getStack().getItem() instanceof BannerItem) {
                    mc.interactionManager.clickSlot(handler.syncId, i, 0, SlotActionType.QUICK_MOVE, mc.player);
                    timer.reset();
                    return;
                }
            }
        }

        // Fill dye slot 1
        if (!loomHandler.slots.get(1).hasStack()) {
            for (int i = invStart; i < containerSize; i++) {
                var slot = loomHandler.slots.get(i);
                if (slot.hasStack() && slot.getStack().getItem() instanceof DyeItem) {
                    mc.interactionManager.clickSlot(handler.syncId, i, 0, SlotActionType.QUICK_MOVE, mc.player);
                    timer.reset();
                    return;
                }
            }
        }

        // Fill pattern slot 2 if available
        if (!loomHandler.slots.get(2).hasStack()) {
            for (int i = invStart; i < containerSize; i++) {
                var slot = loomHandler.slots.get(i);
                if (slot.hasStack() && isBannerPattern(slot.getStack().getItem())) {
                    mc.interactionManager.clickSlot(handler.syncId, i, 0, SlotActionType.QUICK_MOVE, mc.player);
                    timer.reset();
                    return;
                }
            }
        }

        timer.reset();
    }

    private boolean isBannerPattern(net.minecraft.item.Item item) {
        return item == Items.CREEPER_BANNER_PATTERN
                || item == Items.SKULL_BANNER_PATTERN
                || item == Items.FLOWER_BANNER_PATTERN
                || item == Items.MOJANG_BANNER_PATTERN
                || item == Items.GLOBE_BANNER_PATTERN
                || item == Items.PIGLIN_BANNER_PATTERN;
    }
}
