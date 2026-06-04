package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.ModeSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.block.BedBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class AutoBed extends Module {

    private final ModeSetting mode = register(new ModeSetting(
            "Mode", "How to interact with beds",
            "UseExisting", "UseExisting", "PlaceAndUse"));
    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Search range for beds", 4.0, 1.0, 8.0));
    private final BoolSetting nightOnly = register(new BoolSetting(
            "Night Only", "Only activate when sky is dark enough", true));

    private final TimerUtil timer = new TimerUtil();

    public AutoBed() {
        super("AutoBed", "Automatically sleeps in nearby beds or places and uses a bed from inventory", Category.WORLD);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(2000)) return;

        if (nightOnly.isEnabled() && !mc.world.isNight()) return;

        if (mode.is("UseExisting")) {
            if (tryUseExisting()) timer.reset();
        } else {
            // PlaceAndUse: first try an existing bed, else place one from inventory
            if (!tryUseExisting()) {
                if (tryPlaceBed()) timer.reset();
            } else {
                timer.reset();
            }
        }
    }

    /** Right-click the closest bed block in range. Returns true if one was found. */
    private boolean tryUseExisting() {
        int r = (int) range.get();
        BlockPos center = mc.player.getBlockPos();
        for (BlockPos pos : BlockPos.iterate(center.add(-r, -1, -r), center.add(r, 1, r))) {
            if (!(mc.world.getBlockState(pos).getBlock() instanceof BedBlock)) continue;
            if (mc.player.squaredDistanceTo(Vec3d.ofCenter(pos)) > range.get() * range.get()) continue;
            BlockHitResult hit = new BlockHitResult(Vec3d.ofCenter(pos), Direction.UP, pos.toImmutable(), false);
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
            return true;
        }
        return false;
    }

    /**
     * Find a bed item in the hotbar, switch to it, and place it on the block
     * directly in front of the player at foot level. Returns true on success.
     */
    private boolean tryPlaceBed() {
        // Find bed in hotbar
        int bedSlot = -1;
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() instanceof BlockItem __bi && __bi.getBlock() instanceof BedBlock) {
                bedSlot = i;
                break;
            }
        }
        if (bedSlot == -1) return false;

        // Switch to the bed slot
        mc.player.getInventory().selectedSlot = bedSlot;

        // Place on the block one step forward at foot level
        Vec3d look = mc.player.getRotationVector();
        BlockPos placeOn = mc.player.getBlockPos().add(
                (int) Math.signum(look.x), -1, (int) Math.signum(look.z));
        if (!mc.world.getBlockState(placeOn).isSolidBlock(mc.world, placeOn)) return false;
        BlockPos above = placeOn.up();
        if (!mc.world.getBlockState(above).isAir()) return false;

        BlockHitResult hit = new BlockHitResult(
                Vec3d.ofCenter(placeOn).add(0, 0.5, 0), Direction.UP, placeOn, false);
        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
        return true;
    }
}
