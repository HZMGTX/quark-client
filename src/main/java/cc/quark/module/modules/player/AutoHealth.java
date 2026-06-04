package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;

public class AutoHealth extends Module {

    private final DoubleSetting threshold = register(new DoubleSetting(
            "Threshold", "Use health item when HP drops to or below this (hearts)", 8.0, 1.0, 20.0));

    private final BoolSetting gapple = register(new BoolSetting(
            "GoldenApple", "Use golden apple when below threshold", true));

    private final BoolSetting pot = register(new BoolSetting(
            "Potion", "Use health potion when below threshold", true));

    private final TimerUtil timer = new TimerUtil();
    private static final long COOLDOWN_MS = 500;

    public AutoHealth() {
        super("AutoHealth", "Auto-uses health items (gapple, potion) below threshold", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(COOLDOWN_MS)) return;

        float hp = mc.player.getHealth();
        if (hp > (float) threshold.get()) return;

        // Try potion first, then gapple
        if (pot.isEnabled() && tryUseItem(Items.SPLASH_POTION, Items.POTION)) {
            timer.reset();
            return;
        }
        if (gapple.isEnabled() && tryUseItem(Items.ENCHANTED_GOLDEN_APPLE, Items.GOLDEN_APPLE)) {
            timer.reset();
        }
    }

    private boolean tryUseItem(net.minecraft.item.Item... items) {
        var inv = mc.player.getInventory();

        for (net.minecraft.item.Item item : items) {
            // Check hotbar
            for (int i = 0; i < 9; i++) {
                ItemStack stack = inv.getStack(i);
                if (stack.getItem() == item) {
                    int prevSlot = inv.selectedSlot;
                    inv.selectedSlot = i;
                    mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                    inv.selectedSlot = prevSlot;
                    return true;
                }
            }

            // Check main inventory and swap to hotbar slot 8
            for (int i = 9; i < 36; i++) {
                ItemStack stack = inv.getStack(i);
                if (stack.getItem() == item) {
                    mc.interactionManager.clickSlot(
                            mc.player.currentScreenHandler.syncId,
                            i, 8, SlotActionType.SWAP, mc.player);
                    int prevSlot = inv.selectedSlot;
                    inv.selectedSlot = 8;
                    mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                    inv.selectedSlot = prevSlot;
                    return true;
                }
            }
        }
        return false;
    }
}
