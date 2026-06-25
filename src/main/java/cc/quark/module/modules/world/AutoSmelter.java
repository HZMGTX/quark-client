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
 * AutoSmelter — automatically deposits smeltable items from the player's
 * inventory into nearby furnaces and collects the results.
 *
 * Settings:
 *   Range        — scan radius for furnaces.
 *   Auto Deposit — move smeltable items from inventory into furnace input slot.
 *   Auto Collect — collect finished output from furnace output slot.
 *   Auto Fuel    — insert fuel when the fuel slot is empty.
 *   Delay        — ticks between interactions.
 */
public class AutoSmelter extends Module {

    private final IntSetting  range       = register(new IntSetting("Range",        "Furnace scan radius",                   4,  1, 8));
    private final BoolSetting autoDeposit = register(new BoolSetting("Auto Deposit","Deposit ores/food into furnace input.",  true));
    private final BoolSetting autoCollect = register(new BoolSetting("Auto Collect","Collect smelted output automatically.", true));
    private final BoolSetting autoFuel    = register(new BoolSetting("Auto Fuel",   "Add fuel if furnace fuel slot is empty.",true));
    private final IntSetting  delay       = register(new IntSetting("Delay",        "Ticks between actions",                  10, 1, 40));

    private final TimerUtil timer = new TimerUtil();

    private static final int SLOT_INPUT  = 0;
    private static final int SLOT_FUEL   = 1;
    private static final int SLOT_OUTPUT = 2;

    public AutoSmelter() {
        super("AutoSmelter", "Auto-deposits, fuels, and collects from nearby furnaces.", Category.WORLD);
    }

    @Override
    public void onEnable() { timer.reset(); }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(delay.get() * 50L)) return;
        timer.reset();

        // If the player has a furnace handler open, manage it
        if (mc.player.currentScreenHandler instanceof AbstractFurnaceScreenHandler handler) {
            int syncId = handler.syncId;

            if (autoCollect.isEnabled()) {
                ItemStack output = handler.getSlot(SLOT_OUTPUT).getStack();
                if (!output.isEmpty()) {
                    mc.interactionManager.clickSlot(syncId, SLOT_OUTPUT, 0, SlotActionType.QUICK_MOVE, mc.player);
                    return;
                }
            }

            if (autoFuel.isEnabled() && handler.getSlot(SLOT_FUEL).getStack().isEmpty()) {
                for (int i = 0; i < mc.player.getInventory().size(); i++) {
                    ItemStack inv = mc.player.getInventory().getStack(i);
                    if (!inv.isEmpty() && isFuel(inv)) {
                        int screenSlot = invToScreen(i);
                        if (screenSlot >= 0) {
                            mc.interactionManager.clickSlot(syncId, screenSlot, 0, SlotActionType.QUICK_MOVE, mc.player);
                            return;
                        }
                    }
                }
            }

            if (autoDeposit.isEnabled() && handler.getSlot(SLOT_INPUT).getStack().isEmpty()) {
                for (int i = 0; i < mc.player.getInventory().size(); i++) {
                    ItemStack inv = mc.player.getInventory().getStack(i);
                    if (!inv.isEmpty() && isSmeltable(inv)) {
                        int screenSlot = invToScreen(i);
                        if (screenSlot >= 0) {
                            mc.interactionManager.clickSlot(syncId, screenSlot, 0, SlotActionType.QUICK_MOVE, mc.player);
                            return;
                        }
                    }
                }
            }

            return;
        }

        // Scan for and open nearby furnaces that need attention
        int r = range.get();
        BlockPos center = mc.player.getBlockPos();
        for (BlockPos pos : BlockPos.iterate(center.add(-r, -r, -r), center.add(r, r, r))) {
            if (!mc.world.getBlockState(pos).isOf(Blocks.FURNACE) &&
                !mc.world.getBlockState(pos).isOf(Blocks.BLAST_FURNACE) &&
                !mc.world.getBlockState(pos).isOf(Blocks.SMOKER)) continue;

            BlockEntity be = mc.world.getBlockEntity(pos);
            if (!(be instanceof AbstractFurnaceBlockEntity furnace)) continue;

            boolean needsAttention =
                    (autoCollect.isEnabled() && !furnace.getStack(SLOT_OUTPUT).isEmpty()) ||
                    (autoFuel.isEnabled()    &&  furnace.getStack(SLOT_FUEL).isEmpty()) ||
                    (autoDeposit.isEnabled() &&  furnace.getStack(SLOT_INPUT).isEmpty() && hasSmeltable());
            if (!needsAttention) continue;

            BlockHitResult hit = new BlockHitResult(Vec3d.ofCenter(pos), Direction.UP, pos.toImmutable(), false);
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
            return;
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static boolean isFuel(ItemStack stack) {
        var item = stack.getItem();
        return item == Items.COAL || item == Items.CHARCOAL
                || item == Items.OAK_LOG || item == Items.BIRCH_LOG
                || item == Items.SPRUCE_LOG || item == Items.JUNGLE_LOG
                || item == Items.ACACIA_LOG || item == Items.DARK_OAK_LOG
                || item == Items.MANGROVE_LOG || item == Items.CHERRY_LOG
                || item == Items.LAVA_BUCKET || item == Items.BLAZE_ROD
                || item == Items.COAL_BLOCK || item == Items.DRIED_KELP_BLOCK;
    }

    private static boolean isSmeltable(ItemStack stack) {
        var item = stack.getItem();
        return item == Items.RAW_IRON || item == Items.RAW_GOLD || item == Items.RAW_COPPER
                || item == Items.IRON_ORE || item == Items.GOLD_ORE || item == Items.COPPER_ORE
                || item == Items.DEEPSLATE_IRON_ORE || item == Items.DEEPSLATE_GOLD_ORE
                || item == Items.DEEPSLATE_COPPER_ORE
                || item == Items.SAND || item == Items.COBBLESTONE || item == Items.STONE
                || item == Items.CHICKEN || item == Items.BEEF || item == Items.PORKCHOP
                || item == Items.MUTTON || item == Items.RABBIT || item == Items.SALMON
                || item == Items.COD || item == Items.POTATO;
    }

    private boolean hasSmeltable() {
        if (mc.player == null) return false;
        for (int i = 0; i < mc.player.getInventory().size(); i++) {
            if (isSmeltable(mc.player.getInventory().getStack(i))) return true;
        }
        return false;
    }

    private static int invToScreen(int invSlot) {
        if (invSlot < 9)  return 30 + invSlot;
        if (invSlot < 36) return 3 + (invSlot - 9);
        return -1;
    }
}
