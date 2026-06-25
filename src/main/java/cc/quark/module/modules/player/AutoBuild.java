package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class AutoBuild extends Module {

    private final BoolSetting load = register(new BoolSetting(
            "Load", "Enable schematic auto-build", true));

    // Simple 3x3 platform preset
    private static final int[][] PRESET = {
        {-1,0,-1},{0,0,-1},{1,0,-1},
        {-1,0,0}, {0,0,0}, {1,0,0},
        {-1,0,1}, {0,0,1}, {1,0,1}
    };

    private int buildIndex = 0;
    private long lastPlace = 0;

    public AutoBuild() {
        super("AutoBuild", "Auto-builds from schematic", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        buildIndex = 0;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (!load.isEnabled()) return;
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (System.currentTimeMillis() - lastPlace < 100) return;
        if (buildIndex >= PRESET.length) { buildIndex = 0; return; }

        // Find block item in hotbar
        int slot = -1;
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() instanceof BlockItem) {
                slot = i; break;
            }
        }
        if (slot == -1) return;

        int[] offset = PRESET[buildIndex];
        BlockPos target = mc.player.getBlockPos().add(offset[0], offset[1] - 1, offset[2]);

        if (mc.world.getBlockState(target).isAir()) {
            BlockPos below = target.down();
            if (!mc.world.getBlockState(below).isAir()) {
                int prev = mc.player.getInventory().selectedSlot;
                mc.player.getInventory().selectedSlot = slot;
                mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND,
                        new BlockHitResult(Vec3d.ofCenter(below), Direction.UP, below, false));
                mc.player.getInventory().selectedSlot = prev;
                lastPlace = System.currentTimeMillis();
            }
        }
        buildIndex++;
    }
}
