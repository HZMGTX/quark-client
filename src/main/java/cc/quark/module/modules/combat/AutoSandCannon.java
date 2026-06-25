package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class AutoSandCannon extends Module {

    private final DoubleSetting range  = register(new DoubleSetting("Range",  "Target range in blocks", 3.0,  1.0, 8.0));
    private final IntSetting    height = register(new IntSetting("Height",    "Number of sand/gravel blocks to stack", 5, 1, 20));

    private int  placeCount   = 0;
    private long lastPlaceMs  = 0L;

    public AutoSandCannon() {
        super("AutoSandCannon", "Builds sand cannon automatically", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        placeCount  = 0;
        lastPlaceMs = 0L;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (System.currentTimeMillis() - lastPlaceMs < 100L) return;

        // Find nearest player
        PlayerEntity target = null;
        double bestDist = range.get();
        for (Entity e : mc.world.getEntities()) {
            if (e == mc.player) continue;
            if (!(e instanceof PlayerEntity p)) continue;
            double d = mc.player.distanceTo(p);
            if (d < bestDist) {
                bestDist = d;
                target   = p;
            }
        }

        if (target == null) {
            placeCount = 0;
            return;
        }

        if (placeCount >= height.get()) {
            placeCount = 0;
            return;
        }

        // Find sand or gravel in hotbar
        int slot = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack s = mc.player.getInventory().getStack(i);
            if (s.isOf(Items.SAND) || s.isOf(Items.GRAVEL) || s.isOf(Items.RED_SAND)) {
                slot = i;
                break;
            }
        }
        if (slot < 0) return;

        // Place block above target's head
        BlockPos placePos = target.getBlockPos().up(placeCount + 2);

        // Make sure position is accessible and air
        if (!mc.world.getBlockState(placePos).isAir()) {
            placeCount++;
            return;
        }

        // Switch to sand slot
        mc.player.getInventory().selectedSlot = slot;

        // Place via interaction manager
        BlockPos below = placePos.down();
        if (!mc.world.getBlockState(below).isAir()) {
            Vec3d hitVec = Vec3d.ofCenter(below).add(0, 0.5, 0);
            BlockHitResult hitResult = new BlockHitResult(hitVec, Direction.UP, below, false);
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hitResult);
            placeCount++;
            lastPlaceMs = System.currentTimeMillis();
        }
    }
}
