package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.EntityUtil;
import cc.quark.util.TimerUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.Comparator;
import java.util.List;

public class EndCrystal2 extends Module {

    private final DoubleSetting range = register(new DoubleSetting("Range", "Range to place/explode crystals", 4.0, 2.0, 6.0));
    private final IntSetting delay = register(new IntSetting("Delay", "Milliseconds between crystal actions", 300, 50, 2000));
    private final BoolSetting explodeSelf = register(new BoolSetting("Explode Self", "Also explode crystals that may damage self", false));

    private final TimerUtil timer = new TimerUtil();

    public EndCrystal2() {
        super("EndCrystal2", "Enhanced end crystal placer for PvP", Category.COMBAT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(delay.get())) return;

        List<LivingEntity> targets = EntityUtil.getEntitiesOfType(LivingEntity.class, range.get());
        targets.removeIf(e -> !(e instanceof PlayerEntity));
        targets.removeIf(EntityUtil::isFriend);

        // First: try to explode nearby crystals near enemy
        for (var entity : mc.world.getEntities()) {
            if (!(entity instanceof EndCrystalEntity crystal)) continue;
            double distToCrystal = EntityUtil.distanceTo(crystal);
            if (distToCrystal > range.get()) continue;

            boolean nearEnemy = targets.stream()
                    .anyMatch(t -> t.squaredDistanceTo(crystal) < (range.get() * range.get()));

            if (nearEnemy || explodeSelf.isEnabled()) {
                mc.interactionManager.attackEntity(mc.player, crystal);
                mc.player.swingHand(Hand.MAIN_HAND);
                timer.reset();
                return;
            }
        }

        // Second: place crystal near enemy
        if (targets.isEmpty()) return;
        targets.sort(Comparator.comparingDouble(EntityUtil::distanceTo));
        LivingEntity target = targets.get(0);

        int crystalSlot = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() == Items.END_CRYSTAL) {
                crystalSlot = i;
                break;
            }
        }
        if (crystalSlot == -1) return;

        BlockPos targetBase = BlockPos.ofFloored(target.getPos());
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                BlockPos placeBase = targetBase.add(dx, -1, dz);
                BlockPos placeOn = placeBase;
                if (!mc.world.getBlockState(placeOn).isSolid()) continue;
                BlockPos above = placeOn.up();
                if (!mc.world.getBlockState(above).isAir()) continue;
                Vec3d pos = Vec3d.ofCenter(above);
                if (mc.player.getEyePos().distanceTo(pos) > range.get()) continue;

                int prevSlot = mc.player.getInventory().selectedSlot;
                mc.player.getInventory().selectedSlot = crystalSlot;

                BlockHitResult hit = new BlockHitResult(
                        Vec3d.ofCenter(placeOn).add(0, 0.5, 0), Direction.UP, placeOn, false);
                mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
                mc.player.swingHand(Hand.MAIN_HAND);

                mc.player.getInventory().selectedSlot = prevSlot;
                timer.reset();
                return;
            }
        }
    }
}
