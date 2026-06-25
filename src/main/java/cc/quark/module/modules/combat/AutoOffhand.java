package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;

public class AutoOffhand extends Module {

    private static final int OFFHAND_SLOT = 45;

    private final BoolSetting preferTotem = register(new BoolSetting(
            "Prefer Totem", "Keep a totem of undying in offhand when health is critical", true));

    private final BoolSetting useShield = register(new BoolSetting(
            "Use Shield", "Hold a shield in offhand when no higher-priority item is needed", false));

    private final BoolSetting useGap = register(new BoolSetting(
            "Use Gap", "Hold a golden apple in offhand when not in combat", false));

    private final TimerUtil swapTimer = new TimerUtil();

    public AutoOffhand() {
        super("AutoOffhand", "Manages the offhand slot: totem > shield > gap based on situation", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        swapTimer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!swapTimer.hasReached(200)) return;

        int targetSlot = -1;

        if (preferTotem.isEnabled()) {
            float hp = mc.player.getHealth() + mc.player.getAbsorptionAmount();
            // Always keep totem at critically low health; otherwise check existing offhand
            if (hp <= 14f || mc.player.getOffHandStack().getItem() != Items.TOTEM_OF_UNDYING) {
                targetSlot = findInHotbar(Items.TOTEM_OF_UNDYING);
                if (targetSlot == -1) targetSlot = findInInventory(Items.TOTEM_OF_UNDYING);
            }
        }

        if (targetSlot == -1 && useShield.isEnabled()) {
            if (!mc.player.getOffHandStack().isOf(Items.SHIELD)) {
                targetSlot = findInHotbar(Items.SHIELD);
                if (targetSlot == -1) targetSlot = findInInventory(Items.SHIELD);
            }
        }

        if (targetSlot == -1 && useGap.isEnabled()) {
            if (!mc.player.getOffHandStack().isOf(Items.GOLDEN_APPLE)
                    && !mc.player.getOffHandStack().isOf(Items.ENCHANTED_GOLDEN_APPLE)) {
                targetSlot = findInHotbar(Items.ENCHANTED_GOLDEN_APPLE);
                if (targetSlot == -1) targetSlot = findInHotbar(Items.GOLDEN_APPLE);
                if (targetSlot == -1) targetSlot = findInInventory(Items.ENCHANTED_GOLDEN_APPLE);
                if (targetSlot == -1) targetSlot = findInInventory(Items.GOLDEN_APPLE);
            }
        }

        if (targetSlot == -1) return;

        int syncId = mc.player.playerScreenHandler.syncId;
        mc.interactionManager.clickSlot(syncId, targetSlot, 0, SlotActionType.PICKUP, mc.player);
        mc.interactionManager.clickSlot(syncId, OFFHAND_SLOT, 0, SlotActionType.PICKUP, mc.player);
        if (!mc.player.currentScreenHandler.getCursorStack().isEmpty()) {
            mc.interactionManager.clickSlot(syncId, targetSlot, 0, SlotActionType.PICKUP, mc.player);
        }
        swapTimer.reset();
    }

    private int findInHotbar(net.minecraft.item.Item item) {
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).isOf(item)) return i;
        }
        return -1;
    }

    // Inventory slots 9–35 map to playerScreenHandler slots 9–35
    private int findInInventory(net.minecraft.item.Item item) {
        for (int i = 9; i <= 35; i++) {
            if (mc.player.playerScreenHandler.getSlot(i).getStack().isOf(item)) return i;
        }
        return -1;
    }
}
