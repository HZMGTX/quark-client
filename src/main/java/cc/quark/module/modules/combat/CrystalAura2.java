package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class CrystalAura2 extends Module {

    private final DoubleSetting placeRange = register(new DoubleSetting(
            "Place Range", "Range to place end crystals", 4.0, 3.0, 6.0));

    private final DoubleSetting explodeRange = register(new DoubleSetting(
            "Explode Range", "Range to detonate end crystals", 4.0, 3.0, 6.0));

    private final IntSetting delay = register(new IntSetting(
            "Delay", "Milliseconds between place-explode cycles", 100, 1, 500));

    private final TimerUtil timer = new TimerUtil();
    private int prevSlot = -1;

    public CrystalAura2() {
        super("CrystalAura2", "Places and detonates end crystals on nearby players", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        timer.reset();
        prevSlot = -1;
    }

    @Override
    public void onDisable() {
        restoreSlot();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        // Always try to break nearby crystals
        for (Entity ent : mc.world.getEntities()) {
            if (!(ent instanceof EndCrystalEntity crystal)) continue;
            if (mc.player.distanceTo(crystal) > explodeRange.get()) continue;
            mc.interactionManager.attackEntity(mc.player, crystal);
            mc.player.swingHand(Hand.MAIN_HAND);
            break;
        }

        if (!timer.hasReached(delay.get())) return;

        int crystalSlot = findCrystalSlot();
        if (crystalSlot == -1) return;

        BlockPos place = findPlacementPos();
        if (place == null) return;

        int cur = mc.player.getInventory().selectedSlot;
        if (cur != crystalSlot) {
            prevSlot = cur;
            mc.player.getInventory().selectedSlot = crystalSlot;
        }

        Vec3d hitVec = Vec3d.ofCenter(place).add(0, 0.5, 0);
        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND,
                new BlockHitResult(hitVec, Direction.UP, place, false));
        mc.player.swingHand(Hand.MAIN_HAND);
        restoreSlot();
        timer.reset();
    }

    private int findCrystalSlot() {
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).isOf(Items.END_CRYSTAL)) return i;
        }
        return -1;
    }

    private BlockPos findPlacementPos() {
        int r = (int) Math.ceil(placeRange.get());
        BlockPos origin = mc.player.getBlockPos();
        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                for (int dy = -1; dy <= 1; dy++) {
                    BlockPos base = origin.add(dx, dy, dz);
                    if (mc.player.getPos().distanceTo(Vec3d.ofCenter(base)) > placeRange.get()) continue;
                    var state = mc.world.getBlockState(base);
                    if (!state.isOf(Blocks.OBSIDIAN) && !state.isOf(Blocks.BEDROCK)) continue;
                    BlockPos above = base.up();
                    if (mc.world.getBlockState(above).isAir()
                            && mc.world.getBlockState(above.up()).isAir()) {
                        return base;
                    }
                }
            }
        }
        return null;
    }

    private void restoreSlot() {
        if (prevSlot != -1 && mc.player != null) {
            mc.player.getInventory().selectedSlot = prevSlot;
            prevSlot = -1;
        }
    }
}
