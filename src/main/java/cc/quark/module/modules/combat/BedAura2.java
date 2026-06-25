package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.EntityUtil;
import cc.quark.util.TimerUtil;
import net.minecraft.block.BedBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Comparator;
import java.util.List;

public class BedAura2 extends Module {

    private final DoubleSetting range = register(new DoubleSetting("Range", "Range to place beds near enemies", 4.0, 2.0, 6.0));
    private final IntSetting delay = register(new IntSetting("Delay", "Milliseconds between bed placements", 500, 100, 2000));

    private final TimerUtil timer = new TimerUtil();

    public BedAura2() {
        super("BedAura2", "Auto-places and triggers beds in the Nether", Category.COMBAT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(delay.get())) return;

        // Check if we're in nether or end (beds explode there)
        boolean explosive = mc.world.getDimension().ultrawarm()
                || mc.world.getDimension().hasFixedTime();
        if (!explosive) return;

        List<LivingEntity> targets = EntityUtil.getEntitiesOfType(LivingEntity.class, range.get());
        targets.removeIf(e -> !(e instanceof PlayerEntity));
        targets.removeIf(EntityUtil::isFriend);
        if (targets.isEmpty()) return;

        targets.sort(Comparator.comparingDouble(EntityUtil::distanceTo));
        LivingEntity target = targets.get(0);

        // Find bed in hotbar
        int bedSlot = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() instanceof BlockItem __bi && __bi.getBlock() instanceof BedBlock) {
                bedSlot = i;
                break;
            }
        }
        if (bedSlot == -1) return;

        // Find a solid block near target to place bed on
        BlockPos targetBase = BlockPos.ofFloored(target.getPos());
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                BlockPos below = targetBase.add(dx, -1, dz);
                BlockPos placePos = below.up();
                if (!mc.world.getBlockState(below).isSolid()) continue;
                if (!mc.world.getBlockState(placePos).isAir()) continue;
                if (mc.player.getEyePos().distanceTo(Vec3d.ofCenter(placePos)) > range.get()) continue;

                int prevSlot = mc.player.getInventory().selectedSlot;
                mc.player.getInventory().selectedSlot = bedSlot;

                // Place bed
                BlockHitResult hitResult = new BlockHitResult(
                        Vec3d.ofCenter(below).add(0, 0.5, 0), Direction.UP, below, false);
                mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hitResult);

                // Immediately right-click to trigger explosion
                BlockHitResult explodeHit = new BlockHitResult(
                        Vec3d.ofCenter(placePos), Direction.UP, placePos, false);
                mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, explodeHit);

                mc.player.getInventory().selectedSlot = prevSlot;
                timer.reset();
                return;
            }
        }
    }
}
