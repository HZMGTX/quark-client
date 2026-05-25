package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.block.Blocks;
import net.minecraft.item.*;

public class AutoDrop extends Module {

    private final BoolSetting dropRotten = register(new BoolSetting("Rotten Flesh", "Drop rotten flesh", true));
    private final BoolSetting dropBones  = register(new BoolSetting("Bones", "Drop bones", false));
    private final BoolSetting dropArrows = register(new BoolSetting("Arrows", "Drop arrows when full", false));
    private final BoolSetting dropGravel = register(new BoolSetting("Gravel", "Drop gravel", false));
    private final BoolSetting dropDirt   = register(new BoolSetting("Dirt", "Drop dirt", false));

    private int ticker = 0;

    public AutoDrop() {
        super("AutoDrop", "Automatically drops configured junk items from inventory", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (++ticker < 10) return;
        ticker = 0;

        for (int i = 0; i < 36; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty() || !shouldDrop(stack)) continue;
            mc.player.dropItem(stack.copy(), false);
            mc.player.getInventory().setStack(i, ItemStack.EMPTY);
        }
    }

    private boolean shouldDrop(ItemStack stack) {
        Item item = stack.getItem();
        if (dropRotten.isEnabled() && item == Items.ROTTEN_FLESH) return true;
        if (dropBones.isEnabled()  && item == Items.BONE)         return true;
        if (dropArrows.isEnabled() && item == Items.ARROW)        return true;
        if (dropGravel.isEnabled() && item instanceof BlockItem bi
                && bi.getBlock() == Blocks.GRAVEL) return true;
        if (dropDirt.isEnabled()   && item instanceof BlockItem bi
                && bi.getBlock() == Blocks.DIRT)   return true;
        return false;
    }
}
