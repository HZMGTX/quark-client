package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.item.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

public class SurvivalKit extends Module {

    private final DoubleSetting hpThreshold = register(new DoubleSetting("HP Threshold", "Health to trigger kit usage", 4.0, 1.0, 18.0));
    private final BoolSetting useTotem = register(new BoolSetting("Use Totem", "Auto-hold totem of undying in off-hand", true));
    private final BoolSetting useGapple = register(new BoolSetting("Use Gapple", "Auto-eat golden apple when low", true));

    private final TimerUtil timer = new TimerUtil();

    public SurvivalKit() {
        super("SurvivalKit", "Auto-uses totem/gapple below health threshold", Category.COMBAT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(500)) return;

        float hp = mc.player.getHealth();

        if (useTotem.isEnabled()) {
            // Place totem in off-hand if not already there
            ItemStack offHand = mc.player.getOffHandStack();
            if (offHand.getItem() != Items.TOTEM_OF_UNDYING) {
                for (int i = 0; i < mc.player.getInventory().size(); i++) {
                    ItemStack stack = mc.player.getInventory().getStack(i);
                    if (stack.getItem() == Items.TOTEM_OF_UNDYING) {
                        // Swap to off-hand via inventory interaction
                        mc.interactionManager.clickSlot(
                                mc.player.currentScreenHandler.syncId, i, 0,
                                net.minecraft.screen.slot.SlotActionType.PICKUP, mc.player);
                        mc.interactionManager.clickSlot(
                                mc.player.currentScreenHandler.syncId, 45, 0,
                                net.minecraft.screen.slot.SlotActionType.PICKUP, mc.player);
                        mc.interactionManager.clickSlot(
                                mc.player.currentScreenHandler.syncId, i, 0,
                                net.minecraft.screen.slot.SlotActionType.PICKUP, mc.player);
                        timer.reset();
                        return;
                    }
                }
            }
        }

        if (useGapple.isEnabled() && hp <= hpThreshold.get()) {
            for (int i = 0; i < 9; i++) {
                ItemStack stack = mc.player.getInventory().getStack(i);
                if (stack.getItem() == Items.GOLDEN_APPLE || stack.getItem() == Items.ENCHANTED_GOLDEN_APPLE) {
                    mc.player.getInventory().selectedSlot = i;
                    mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                    timer.reset();
                    return;
                }
            }
        }
    }
}
