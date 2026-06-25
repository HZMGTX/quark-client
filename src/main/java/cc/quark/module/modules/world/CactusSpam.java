package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class CactusSpam extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Placement range for cactus barriers", 2.0, 1.0, 5.0));

    private final IntSetting count = register(new IntSetting(
            "Count", "Number of cactus to place", 5, 1, 20));

    private int placed = 0;
    private long lastPlace = 0;

    public CactusSpam() {
        super("CactusSpam", "Places cactus for defensive barriers", Category.WORLD);
    }

    @Override
    public void onEnable() { placed = 0; }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (placed >= count.get()) { placed = 0; return; }
        if (System.currentTimeMillis() - lastPlace < 200) return;

        int slot = -1;
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == Items.CACTUS) {
                slot = i; break;
            }
        }
        if (slot == -1) return;

        // Place cactus around player
        double angle = (placed / (double) count.get()) * 2 * Math.PI;
        double r = range.get();
        int px = (int)(mc.player.getX() + Math.sin(angle) * r);
        int pz = (int)(mc.player.getZ() + Math.cos(angle) * r);
        BlockPos base = new BlockPos(px, mc.player.getBlockY(), pz);

        if (mc.world.getBlockState(base).getBlock() == Blocks.SAND
                || mc.world.getBlockState(base).isAir()) {
            BlockPos below = base.down();
            if (!mc.world.getBlockState(below).isAir()) {
                int prev = mc.player.getInventory().selectedSlot;
                mc.player.getInventory().selectedSlot = slot;
                mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND,
                        new BlockHitResult(Vec3d.ofCenter(below), Direction.UP, below, false));
                mc.player.getInventory().selectedSlot = prev;
                placed++;
                lastPlace = System.currentTimeMillis();
            }
        }
    }
}
