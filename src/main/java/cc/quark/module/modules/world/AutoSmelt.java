package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.AbstractFurnaceScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

/**
 * AutoSmelt — manages furnaces automatically.
 *
 * When a furnace screen is open:
 *   Auto Fuel    — shifts fuel items into the fuel slot if it is empty.
 *   Auto Collect — shift-clicks the output slot when an item is ready.
 *
 * Scanning: when no furnace screen is open, scans nearby furnaces and opens the
 * nearest one that needs attention.
 */
public class AutoSmelt extends Module {

    private final IntSetting  range       = register(new IntSetting("Range",        "Scan radius for furnaces",          4,  1, 8));
    private final IntSetting  delay       = register(new IntSetting("Delay",        "Ticks between interactions",         10, 1, 40));
    private final BoolSetting autoFuel    = register(new BoolSetting("Auto Fuel",   "Add fuel automatically.",            true));
    private final BoolSetting autoCollect = register(new BoolSetting("Auto Collect","Collect output automatically.",      true));

    private final TimerUtil timer = new TimerUtil();

    // Furnace slot indices (vanilla):
    //   0 = ingredient (input)
    //   1 = fuel
    //   2 = output
    private static final int SLOT_FUEL   = 1;
    private static final int SLOT_OUTPUT = 2;

    public AutoSmelt() {
        super("AutoSmelt", "Manages furnaces automatically.", Category.WORLD);
    }

    @Override
    public void onEnable() { timer.reset(); }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(delay.get() * 50L)) return;
        timer.reset();

        // Check if player currently has an AbstractFurnaceScreenHandler open
        if (mc.player.currentScreenHandler instanceof AbstractFurnaceScreenHandler handler) {
            int syncId = handler.syncId;

            // Collect output first
            if (autoCollect.isEnabled()) {
                ItemStack output = handler.getSlot(SLOT_OUTPUT).getStack();
                if (!output.isEmpty()) {
                    mc.interactionManager.clickSlot(syncId, SLOT_OUTPUT, 0, SlotActionType.QUICK_MOVE, mc.player);
                    return;
                }
            }

            // Add fuel if fuel slot is empty
            if (autoFuel.isEnabled()) {
                ItemStack fuelSlot = handler.getSlot(SLOT_FUEL).getStack();
                if (fuelSlot.isEmpty()) {
                    for (int i = 0; i < mc.player.getInventory().size(); i++) {
                        ItemStack inv = mc.player.getInventory().getStack(i);
                        if (!inv.isEmpty() && isFuel(inv.getItem())) {
                            int screenSlot = inventoryToScreenSlot(i);
                            if (screenSlot >= 0) {
                                mc.interactionManager.clickSlot(syncId, screenSlot, 0, SlotActionType.QUICK_MOVE, mc.player);
                                return;
                            }
                        }
                    }
                }
            }

            return; // handler open but nothing to do
        }

        // No furnace handler open — scan for a nearby furnace and open it
        int r = range.get();
        BlockPos center = mc.player.getBlockPos();
        for (BlockPos pos : BlockPos.iterate(center.add(-r, -r, -r), center.add(r, r, r))) {
            boolean isFurnaceBlock =
                    mc.world.getBlockState(pos).isOf(Blocks.FURNACE) ||
                    mc.world.getBlockState(pos).isOf(Blocks.BLAST_FURNACE) ||
                    mc.world.getBlockState(pos).isOf(Blocks.SMOKER);
            if (!isFurnaceBlock) continue;

            BlockEntity be = mc.world.getBlockEntity(pos);
            if (!(be instanceof AbstractFurnaceBlockEntity furnace)) continue;

            // Only open if there is output to collect or fuel needed
            ItemStack output   = furnace.getStack(SLOT_OUTPUT);
            ItemStack fuelItem = furnace.getStack(SLOT_FUEL);
            boolean needsAttention = (autoCollect.isEnabled() && !output.isEmpty())
                    || (autoFuel.isEnabled() && fuelItem.isEmpty());
            if (!needsAttention) continue;

            BlockHitResult hit = new BlockHitResult(Vec3d.ofCenter(pos), Direction.UP, pos.toImmutable(), false);
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
            return;
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static boolean isFuel(Item item) {
        return item == Items.COAL || item == Items.CHARCOAL
                || item == Items.OAK_LOG || item == Items.BIRCH_LOG
                || item == Items.SPRUCE_LOG || item == Items.JUNGLE_LOG
                || item == Items.ACACIA_LOG || item == Items.DARK_OAK_LOG
                || item == Items.MANGROVE_LOG || item == Items.CHERRY_LOG
                || item == Items.OAK_PLANKS || item == Items.BIRCH_PLANKS
                || item == Items.SPRUCE_PLANKS || item == Items.JUNGLE_PLANKS
                || item == Items.LAVA_BUCKET || item == Items.BLAZE_ROD
                || item == Items.COAL_BLOCK || item == Items.DRIED_KELP_BLOCK;
    }

    /**
     * Map a player inventory slot index to the furnace screen handler slot index.
     * In a furnace screen handler: slots 3-29 = main inventory, 30-38 = hotbar.
     */
    private static int inventoryToScreenSlot(int invSlot) {
        if (invSlot < 9)  return 30 + invSlot;   // hotbar
        if (invSlot < 36) return 3 + (invSlot - 9); // main inventory
        return -1;
    }
}
