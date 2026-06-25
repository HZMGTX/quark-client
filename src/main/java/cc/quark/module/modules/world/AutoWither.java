package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.ChatUtil;
import cc.quark.util.TimerUtil;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

/**
 * AutoWither - Auto-places soul sand/wither skulls to spawn a Wither boss.
 * Expects soul sand and wither skulls in the hotbar.
 *
 * Pattern (relative to origin, facing north):
 *   Soul sand: (0,0,0), (-1,0,0), (1,0,0), (0,1,0)
 *   Skulls   : (-1,1,0), (0,1,0), (1,1,0)  <- actually placed on top of soul sand
 */
public class AutoWither extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Max placement range in blocks", 4.0, 2.0, 6.0));
    private final IntSetting delay = register(new IntSetting(
            "Delay", "Milliseconds between placements", 500, 100, 2000));

    private final TimerUtil timer = new TimerUtil();
    private int stage = 0;

    // Offsets for the wither structure relative to player feet
    // Soul sand layer (y=0): left, center, right
    // Soul sand top (y=1): center
    // Wither skull tops (y=2): left, center, right
    private static final int[][] SOUL_SAND = {{-1, 0, 0}, {0, 0, 0}, {1, 0, 0}, {0, 1, 0}};
    private static final int[][] SKULLS    = {{-1, 2, 0}, {0, 2, 0}, {1, 2, 0}};

    public AutoWither() {
        super("AutoWither", "Auto-places wither skulls to spawn wither", Category.WORLD);
    }

    @Override
    public void onEnable() {
        stage = 0;
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(delay.get())) return;

        if (stage < SOUL_SAND.length) {
            placeBlock(SOUL_SAND[stage], Items.SOUL_SAND);
        } else if (stage < SOUL_SAND.length + SKULLS.length) {
            placeBlock(SKULLS[stage - SOUL_SAND.length], Items.WITHER_SKELETON_SKULL);
        } else {
            ChatUtil.success("AutoWither: structure complete!");
            disable();
            return;
        }
        stage++;
        timer.reset();
    }

    private void placeBlock(int[] offset, net.minecraft.item.Item item) {
        BlockPos base = mc.player.getBlockPos().add(offset[0], offset[1], offset[2]);

        // Check range
        if (mc.player.getPos().distanceTo(Vec3d.ofCenter(base)) > range.get()) return;

        // Find item in hotbar
        int slot = -1;
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).isOf(item)) { slot = i; break; }
        }
        if (slot == -1) { ChatUtil.error("AutoWither: missing " + item.getName().getString()); disable(); return; }

        // Place on block below
        BlockPos support = base.down();
        if (!mc.world.getBlockState(support).isSolidBlock(mc.world, support)) return;

        int prev = mc.player.getInventory().selectedSlot;
        mc.player.getInventory().selectedSlot = slot;
        BlockHitResult hit = new BlockHitResult(Vec3d.ofCenter(support), Direction.UP, support, false);
        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
        mc.player.swingHand(Hand.MAIN_HAND);
        mc.player.getInventory().selectedSlot = prev;
    }
}
