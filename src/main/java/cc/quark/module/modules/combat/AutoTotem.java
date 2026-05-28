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

    private static final int OFFHAND_SLOT = 40;

    private final DoubleSetting healthThreshold = register(new DoubleSetting(
            "Health Threshold", "Move totem to offhand when health is at or below this value", 6.0, 2.0, 16.0));

    private final BoolSetting checkOffhand = register(new BoolSetting(
            "Check Offhand", "Only swap when offhand is empty or not a totem", true));

    private final BoolSetting crystalDetection = register(new BoolSetting(
            "Crystal Detection", "Switch to totem when an End Crystal is within 6 blocks", true));

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

        float health = mc.player.getHealth();
        if (health <= (float) healthThreshold.get()) {
            shouldSwap = true;
        }

        if (!shouldSwap && crystalDetection.isEnabled()) {
            for (Entity entity : mc.world.getEntities()) {
                if (entity instanceof EndCrystalEntity) {
                    if (mc.player.distanceTo(entity) <= 6.0) {
                        shouldSwap = true;
                        break;
                    }
                }
            }
        }

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

        if (checkOffhand.isEnabled() &&
                mc.player.getOffHandStack().getItem() == Items.TOTEM_OF_UNDYING) return;

        int totemSlot = findTotemSlot();
        if (totemSlot == -1) return;

        int syncId = mc.player.playerScreenHandler.syncId;

        mc.interactionManager.clickSlot(syncId, totemSlot, 0, SlotActionType.PICKUP, mc.player);
        mc.interactionManager.clickSlot(syncId, OFFHAND_SLOT, 0, SlotActionType.PICKUP, mc.player);

        if (!mc.player.currentScreenHandler.getCursorStack().isEmpty()) {
            mc.interactionManager.clickSlot(syncId, totemSlot, 0, SlotActionType.PICKUP, mc.player);
        }
    }

    private int findTotemSlot() {
        for (int i = 0; i <= 35; i++) {
            var stack = mc.player.playerScreenHandler.getSlot(i).getStack();
            if (stack.getItem() == Items.TOTEM_OF_UNDYING) {
                return i;
            }
        }
        return -1;
    }
}
