package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import net.minecraft.item.BlockItem;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class RoofBuilder extends Module {

    private final IntSetting width = register(new IntSetting(
            "Width", "Half-width of roof (blocks from center)", 3, 1, 10));

    private final IntSetting delay = register(new IntSetting(
            "Delay", "Milliseconds between placements", 200, 50, 1000));

    private int buildX = 0;
    private int buildZ = 0;
    private long lastPlace = 0;

    public RoofBuilder() {
        super("RoofBuilder", "Builds roof above current location", Category.WORLD);
    }

    @Override
    public void onEnable() { buildX = -width.get(); buildZ = -width.get(); }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (System.currentTimeMillis() - lastPlace < delay.get()) return;

        int w = width.get();
        if (buildX > w) { buildX = -w; buildZ++; }
        if (buildZ > w) { buildZ = -w; return; }

        int slot = -1;
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() instanceof BlockItem) {
                slot = i; break;
            }
        }
        if (slot == -1) return;

        BlockPos target = mc.player.getBlockPos().add(buildX, 3, buildZ);
        BlockPos below = target.down();
        if (mc.world.getBlockState(target).isAir() && !mc.world.getBlockState(below).isAir()) {
            int prev = mc.player.getInventory().selectedSlot;
            mc.player.getInventory().selectedSlot = slot;
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND,
                    new BlockHitResult(Vec3d.ofCenter(below), Direction.UP, below, false));
            mc.player.getInventory().selectedSlot = prev;
            lastPlace = System.currentTimeMillis();
        }
        buildX++;
    }
}
