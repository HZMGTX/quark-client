package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;

public class AutoGapple3 extends Module {

    private final DoubleSetting threshold = register(new DoubleSetting(
            "Threshold", "Eat golden apple when HP drops to or below this (hearts)", 8.0, 1.0, 20.0));

    private final BoolSetting enchanted = register(new BoolSetting(
            "Enchanted", "Prefer enchanted golden apple (notch apple) over regular", false));

    private final IntSetting delay = register(new IntSetting(
            "Delay", "Minimum delay between gapple uses in milliseconds", 500, 100, 10000));

    private final TimerUtil timer = new TimerUtil();
    private int savedSlot = -1;

    public AutoGapple3() {
        super("AutoGapple3", "Auto-eats golden apple when health drops", Category.PLAYER);
    }

    @Override
    public void onDisable() {
        if (mc.player != null && savedSlot != -1) {
            mc.player.getInventory().selectedSlot = savedSlot;
            savedSlot = -1;
        }
        if (mc.options != null) mc.options.useKey.setPressed(false);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(delay.get())) return;

        float hp = mc.player.getHealth();
        if (hp > (float) threshold.get()) {
            // Stop eating if health recovered
            if (mc.player.isUsingItem()) {
                mc.options.useKey.setPressed(false);
            }
            if (savedSlot != -1) {
                mc.player.getInventory().selectedSlot = savedSlot;
                savedSlot = -1;
            }
            return;
        }

        // Find the best gapple
        int slot = enchanted.isEnabled()
                ? findItem(Items.ENCHANTED_GOLDEN_APPLE, Items.GOLDEN_APPLE)
                : findItem(Items.GOLDEN_APPLE, Items.ENCHANTED_GOLDEN_APPLE);

        if (slot == -1) return;

        var inv = mc.player.getInventory();

        if (slot >= 9) {
            // Swap from inventory to hotbar slot 8
            mc.interactionManager.clickSlot(
                    mc.player.currentScreenHandler.syncId,
                    slot, 8, SlotActionType.SWAP, mc.player);
            slot = 8;
        }

        if (savedSlot == -1) savedSlot = inv.selectedSlot;
        inv.selectedSlot = slot;
        mc.options.useKey.setPressed(true);
        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);

        timer.reset();
    }

    private int findItem(net.minecraft.item.Item... items) {
        var inv = mc.player.getInventory();
        for (net.minecraft.item.Item item : items) {
            for (int i = 0; i < 9; i++) {
                if (inv.getStack(i).getItem() == item) return i;
            }
            for (int i = 9; i < 36; i++) {
                if (inv.getStack(i).getItem() == item) return i;
            }
        }
        return -1;
    }
}
