package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.util.ChatUtil;
import cc.quark.util.TimerUtil;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.screen.BrewingStandScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.BlockPos;

public class BrewAssist extends Module {

    private final BoolSetting autoNether = register(new BoolSetting(
            "AutoNether", "Alert when nether wart is needed for brewing", true));
    private final BoolSetting autoBlaze = register(new BoolSetting(
            "AutoBlaze", "Auto-insert blaze powder as fuel when brewing stand is open", true));

    private final TimerUtil timer = new TimerUtil();

    public BrewAssist() {
        super("BrewAssist", "Assists with potion brewing: auto-fuels and alerts about missing ingredients", Category.WORLD);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(500)) return;
        timer.reset();

        // Check if brewing stand screen is open
        if (!(mc.player.currentScreenHandler instanceof BrewingStandScreenHandler brewHandler)) {
            checkNearbyBrewingStands();
            return;
        }

        // Auto-fuel: slot 4 = blaze powder fuel
        if (autoBlaze.isEnabled()) {
            var fuelSlot = brewHandler.slots.get(4);
            if (!fuelSlot.hasStack()) {
                int blazeSlot = findItemInInventory(brewHandler, Items.BLAZE_POWDER);
                if (blazeSlot != -1) {
                    mc.interactionManager.clickSlot(
                            brewHandler.syncId, blazeSlot, 0, SlotActionType.QUICK_MOVE, mc.player);
                    return;
                }
            }
        }

        // Auto-fill ingredient slot: slot 3 = ingredient
        var ingSlot = brewHandler.slots.get(3);
        if (!ingSlot.hasStack()) {
            // Check for nether wart (primary ingredient)
            int wartSlot = findItemInInventory(brewHandler, Items.NETHER_WART);
            if (wartSlot != -1) {
                mc.interactionManager.clickSlot(
                        brewHandler.syncId, wartSlot, 0, SlotActionType.QUICK_MOVE, mc.player);
                if (autoNether.isEnabled()) {
                    ChatUtil.info("[BrewAssist] Inserted nether wart into brewing stand.");
                }
            } else if (autoNether.isEnabled()) {
                ChatUtil.warn("[BrewAssist] No nether wart available for brewing!");
            }
        }

        // Fill empty potion slots (0-2) with water bottles
        for (int i = 0; i < 3; i++) {
            if (!brewHandler.slots.get(i).hasStack()) {
                int bottleSlot = findItemInInventory(brewHandler, Items.GLASS_BOTTLE);
                if (bottleSlot == -1) bottleSlot = findItemInInventory(brewHandler, Items.POTION);
                if (bottleSlot != -1) {
                    mc.interactionManager.clickSlot(
                            brewHandler.syncId, bottleSlot, 0, SlotActionType.QUICK_MOVE, mc.player);
                    return;
                }
            }
        }
    }

    private void checkNearbyBrewingStands() {
        if (!autoNether.isEnabled()) return;
        BlockPos center = mc.player.getBlockPos();
        for (BlockPos pos : BlockPos.iterate(center.add(-4, -2, -4), center.add(4, 2, 4))) {
            if (mc.world.getBlockState(pos).isOf(Blocks.BREWING_STAND)) {
                // Check if player has blaze powder
                boolean hasBlaze = false, hasWart = false;
                for (int i = 0; i < 36; i++) {
                    var stack = mc.player.getInventory().getStack(i);
                    if (stack.isOf(Items.BLAZE_POWDER)) hasBlaze = true;
                    if (stack.isOf(Items.NETHER_WART)) hasWart = true;
                }
                if (!hasBlaze) ChatUtil.warn("[BrewAssist] Missing blaze powder for fuel!");
                if (!hasWart)  ChatUtil.warn("[BrewAssist] Missing nether wart for brewing!");
                return;
            }
        }
    }

    private int findItemInInventory(BrewingStandScreenHandler handler, net.minecraft.item.Item item) {
        int size = handler.slots.size();
        for (int i = 5; i < size; i++) {
            if (handler.slots.get(i).hasStack() && handler.slots.get(i).getStack().isOf(item)) {
                return i;
            }
        }
        return -1;
    }
}
