package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.TntEntity;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;

public class AutoTotem extends Module {

    // Slot 45 in the player's screen handler is the offhand slot
    private static final int OFFHAND_SLOT = 45;

    private final DoubleSetting healthThreshold = register(new DoubleSetting(
            "Health Threshold", "Swap totem to offhand when health is at or below this value", 10.0, 1.0, 10.0));

    private final BoolSetting keepSwap = register(new BoolSetting(
            "Keep Swap", "Always keep a totem in offhand regardless of health threshold", false));

    private final BoolSetting crystalDetect = register(new BoolSetting(
            "Crystal Detect", "Preemptively swap when an End Crystal is placed within 6 blocks", true));

    private final BoolSetting explosionDetection = register(new BoolSetting(
            "Explosion Detection", "Switch to totem when TNT or Creeper is nearby", true));

    public AutoTotem() {
        super("AutoTotem", "Automatically moves totems to offhand", Category.COMBAT);
    }

    @Override
    public void onEnable() {}

    @Override
    public void onDisable() {}

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        boolean shouldSwap = false;

        // Keep Swap: always maintain a totem in offhand
        if (keepSwap.isEnabled()) {
            shouldSwap = true;
        }

        // Health threshold check
        if (!shouldSwap) {
            float health = mc.player.getHealth();
            if (health <= (float) healthThreshold.get()) {
                shouldSwap = true;
            }
        }

        // Crystal detection: preemptively swap when crystal is near
        if (!shouldSwap && crystalDetect.isEnabled()) {
            for (Entity entity : mc.world.getEntities()) {
                if (entity instanceof EndCrystalEntity) {
                    if (mc.player.distanceTo(entity) <= 6.0) {
                        shouldSwap = true;
                        break;
                    }
                }
            }
        }

        // Explosion detection: TNT or Creeper nearby
        if (!shouldSwap && explosionDetection.isEnabled()) {
            for (Entity entity : mc.world.getEntities()) {
                boolean isThreat = entity instanceof TntEntity || entity instanceof CreeperEntity;
                if (isThreat && mc.player.distanceTo(entity) <= 6.0) {
                    shouldSwap = true;
                    break;
                }
            }
        }

        if (!shouldSwap) return;

        // Don't swap if offhand already has a totem
        if (mc.player.getOffHandStack().getItem() == Items.TOTEM_OF_UNDYING) return;

        int totemSlot = findTotemSlot();
        if (totemSlot == -1) return;

        int syncId = mc.player.playerScreenHandler.syncId;

        // Pick up the totem from its slot
        mc.interactionManager.clickSlot(syncId, totemSlot, 0, SlotActionType.PICKUP, mc.player);
        // Place it in the offhand slot (slot 45)
        mc.interactionManager.clickSlot(syncId, OFFHAND_SLOT, 0, SlotActionType.PICKUP, mc.player);

        // If the cursor still has an item (offhand was occupied), put it back
        if (!mc.player.currentScreenHandler.getCursorStack().isEmpty()) {
            mc.interactionManager.clickSlot(syncId, totemSlot, 0, SlotActionType.PICKUP, mc.player);
        }
    }

    private int findTotemSlot() {
        // Search entire inventory (slots 0–35 in playerScreenHandler correspond to inventory + hotbar)
        for (int i = 0; i <= 35; i++) {
            var stack = mc.player.playerScreenHandler.getSlot(i).getStack();
            if (stack.getItem() == Items.TOTEM_OF_UNDYING) {
                return i;
            }
        }
        return -1;
    }
}
