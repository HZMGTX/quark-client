package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.block.Blocks;
import net.minecraft.item.*;

public class AutoDrop extends Module {

    private final BoolSetting dropRotten  = register(new BoolSetting("Rotten Flesh", "Drop rotten flesh", true));
    private final BoolSetting dropBones   = register(new BoolSetting("Bones",         "Drop bones", false));
    private final BoolSetting dropArrows  = register(new BoolSetting("Arrows",        "Drop arrows", false));
    private final BoolSetting dropGravel  = register(new BoolSetting("Gravel",        "Drop gravel", false));
    private final BoolSetting dropDirt    = register(new BoolSetting("Dirt",          "Drop dirt", false));
    private final BoolSetting dropSand    = register(new BoolSetting("Sand",          "Drop sand", false));
    private final BoolSetting dropGunpow  = register(new BoolSetting("Gunpowder",     "Drop gunpowder", false));
    private final BoolSetting dropString  = register(new BoolSetting("String",        "Drop string", false));

    private final TimerUtil timer = new TimerUtil();

    public AutoDrop() {
        super("AutoDrop", "Automatically drops configured junk items from inventory", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.interactionManager == null) return;
        if (!timer.hasReached(500)) return;
        timer.reset();

        for (int i = 0; i < 36; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty() || !shouldDrop(stack)) continue;

            // Use throw slot action for proper server sync
            int guiSlot = i < 9 ? 36 + i : i;
            mc.interactionManager.clickSlot(
                    mc.player.currentScreenHandler.syncId,
                    guiSlot, 1, net.minecraft.screen.slot.SlotActionType.THROW, mc.player);
        }
    }

    private boolean shouldDrop(ItemStack stack) {
        Item item = stack.getItem();
        if (dropRotten.isEnabled()  && item == Items.ROTTEN_FLESH)  return true;
        if (dropBones.isEnabled()   && item == Items.BONE)           return true;
        if (dropArrows.isEnabled()  && item == Items.ARROW)          return true;
        if (dropGunpow.isEnabled()  && item == Items.GUNPOWDER)      return true;
        if (dropString.isEnabled()  && item == Items.STRING)         return true;
        if (item instanceof BlockItem bi) {
            if (dropGravel.isEnabled() && bi.getBlock() == Blocks.GRAVEL) return true;
            if (dropDirt.isEnabled()   && bi.getBlock() == Blocks.DIRT)   return true;
            if (dropSand.isEnabled()   && (bi.getBlock() == Blocks.SAND
                                       || bi.getBlock() == Blocks.RED_SAND)) return true;
        }
        return false;
    }

    @Override
    public String getSuffix() {
        if (mc.player == null) return "";
        int count = 0;
        for (int i = 0; i < 36; i++) {
            if (shouldDrop(mc.player.getInventory().getStack(i))) count++;
        }
        return count > 0 ? count + " slots" : "";
    }
}
