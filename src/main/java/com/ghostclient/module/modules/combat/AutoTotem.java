package com.ghostclient.module.modules.combat;

import com.ghostclient.event.EventHandler;
import com.ghostclient.event.events.EventTick;
import com.ghostclient.module.Category;
import com.ghostclient.module.Module;
import com.ghostclient.setting.BoolSetting;
import com.ghostclient.setting.DoubleSetting;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;

/**
 * AutoTotem - automatically moves a Totem of Undying from the player's inventory
 * into the offhand slot whenever health drops at or below a configurable threshold.
 *
 * <p>Offhand slot in PlayerScreenHandler = 40.
 * Hotbar = slots 0-8, main inventory = slots 9-35.
 */
public class AutoTotem extends Module {

    /** Container slot index of the offhand in the player inventory screen. */
    private static final int OFFHAND_SLOT = 40;

    private final DoubleSetting healthThreshold = register(new DoubleSetting(
            "Health Threshold", "Move totem to offhand when health is at or below this value",
            6.0, 1.0, 10.0));

    private final BoolSetting checkOffhand = register(new BoolSetting(
            "Check Offhand", "Only swap when offhand is empty or not a totem", true));

    public AutoTotem() {
        super("AutoTotem", "Automatically moves totems to offhand", Category.COMBAT, 0);
    }

    @Override
    public void onEnable() {}

    @Override
    public void onDisable() {}

    @EventHandler
    public void onTick(EventTick event) {
        MinecraftClient mc = net.minecraft.client.MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        float health = mc.player.getHealth();
        if (health > (float) healthThreshold.get()) return;

        // Check if totem already in offhand
        if (checkOffhand.isEnabled() &&
                mc.player.getOffHandStack().getItem() == Items.TOTEM_OF_UNDYING) return;

        // Find a totem slot in inventory (hotbar 0-8, main 9-35)
        int totemSlot = findTotemSlot(mc);
        if (totemSlot == -1) return;

        int syncId = mc.player.playerScreenHandler.syncId;

        // Pick up the totem from its slot
        mc.interactionManager.clickSlot(syncId, totemSlot, 0, SlotActionType.PICKUP, mc.player);
        // Place it into the offhand slot
        mc.interactionManager.clickSlot(syncId, OFFHAND_SLOT, 0, SlotActionType.PICKUP, mc.player);

        // If the cursor still holds something (the old offhand item), put it back into the totem's original slot
        if (!mc.player.currentScreenHandler.getCursorStack().isEmpty()) {
            mc.interactionManager.clickSlot(syncId, totemSlot, 0, SlotActionType.PICKUP, mc.player);
        }
    }

    /**
     * Scans hotbar (0-8) then main inventory (9-35) for a Totem of Undying.
     * Returns the container slot index, or -1 if none found.
     */
    private int findTotemSlot(net.minecraft.client.MinecraftClient mc) {
        for (int i = 0; i <= 35; i++) {
            var stack = mc.player.playerScreenHandler.getSlot(i).getStack();
            if (stack.getItem() == Items.TOTEM_OF_UNDYING) {
                return i;
            }
        }
        return -1;
    }
}
