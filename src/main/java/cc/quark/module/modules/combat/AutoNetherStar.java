package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.block.BeaconBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

/**
 * AutoNetherStar - automatically places a nether star into a nearby beacon
 * when one is found within reach and the player holds or has a nether star.
 */
public class AutoNetherStar extends Module {

    private final IntSetting range = register(new IntSetting(
            "Range", "Block radius to search for beacons", 5, 1, 10));

    private final BoolSetting autoSwap = register(new BoolSetting(
            "Auto Swap", "Switch to nether star slot automatically", true));

    private boolean activated = false;

    public AutoNetherStar() {
        super("AutoNetherStar", "Automatically activates beacons with a nether star", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        activated = false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (activated) return;

        // Find nether star slot in hotbar
        int starSlot = -1;
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == Items.NETHER_STAR) {
                starSlot = i;
                break;
            }
        }
        if (starSlot == -1) return;

        // Scan for beacon block nearby
        BlockPos playerPos = mc.player.getBlockPos();
        int r = range.get();
        BlockPos target = null;
        for (int x = -r; x <= r && target == null; x++) {
            for (int y = -r; y <= r && target == null; y++) {
                for (int z = -r; z <= r && target == null; z++) {
                    BlockPos candidate = playerPos.add(x, y, z);
                    if (mc.world.getBlockState(candidate).getBlock() instanceof BeaconBlock) {
                        target = candidate;
                    }
                }
            }
        }
        if (target == null) return;

        // Swap to nether star and interact
        int prevSlot = mc.player.getInventory().selectedSlot;
        if (autoSwap.isEnabled()) {
            mc.player.getInventory().selectedSlot = starSlot;
        } else if (mc.player.getInventory().selectedSlot != starSlot) {
            return;
        }

        Vec3d hitVec = Vec3d.ofCenter(target);
        BlockHitResult hit = new BlockHitResult(hitVec, Direction.UP, target, false);
        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
        mc.player.swingHand(Hand.MAIN_HAND);

        if (autoSwap.isEnabled()) {
            mc.player.getInventory().selectedSlot = prevSlot;
        }

        activated = true;
    }
}
