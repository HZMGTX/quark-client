package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.block.Blocks;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.LightType;

/**
 * AutoTorch — automatically places torches in dark areas to prevent
 * hostile mob spawning. The module checks the block-light level at the
 * player's feet and places a torch on the floor when it falls below the
 * configured threshold.
 */
public class AutoTorch extends Module {

    private final IntSetting lightThreshold = register(new IntSetting(
            "Light Threshold", "Place torch when block light level is below this value", 8, 1, 15));

    private final IntSetting cooldownMs = register(new IntSetting(
            "Cooldown", "Milliseconds between torch placements", 500, 100, 5000));

    private final BoolSetting switchBack = register(new BoolSetting(
            "Switch Back", "Return to previous hotbar slot after placing", true));

    private final BoolSetting requireGround = register(new BoolSetting(
            "Require Ground", "Only place torches while on the ground", true));

    private final BoolSetting checkFloor = register(new BoolSetting(
            "Check Floor", "Ensure the floor block can hold a torch before placing", true));

    private final TimerUtil timer = new TimerUtil();

    public AutoTorch() {
        super("AutoTorch", "Automatically places torches in dark areas", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        ClientPlayerEntity player = mc.player;

        if (requireGround.isEnabled() && !player.isOnGround()) return;
        if (!timer.hasReached(cooldownMs.get())) return;

        BlockPos feetPos = player.getBlockPos();

        // Only place if we're in a dark spot
        int lightLevel = mc.world.getLightLevel(LightType.BLOCK, feetPos);
        if (lightLevel >= lightThreshold.get()) return;

        // Make sure there's no block already at feet position
        if (!mc.world.getBlockState(feetPos).isAir()) return;

        // Check if the floor can support a torch
        BlockPos floorPos = feetPos.down();
        if (checkFloor.isEnabled()) {
            if (!mc.world.getBlockState(floorPos).isSolidBlock(mc.world, floorPos)) return;
        }

        // Find a torch in the hotbar
        int torchSlot = findTorchSlot();
        if (torchSlot == -1) return;

        timer.reset();

        int prevSlot = player.getInventory().selectedSlot;
        player.getInventory().selectedSlot = torchSlot;

        // Place the torch on top of the floor block
        Vec3d hitVec = new Vec3d(
                floorPos.getX() + 0.5,
                floorPos.getY() + 1.0,
                floorPos.getZ() + 0.5);
        BlockHitResult hit = new BlockHitResult(hitVec, Direction.UP, floorPos, false);
        mc.interactionManager.interactBlock(player, Hand.MAIN_HAND, hit);
        player.swingHand(Hand.MAIN_HAND);

        if (switchBack.isEnabled()) {
            player.getInventory().selectedSlot = prevSlot;
        }
    }

    private int findTorchSlot() {
        if (mc.player == null) return -1;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() == Items.TORCH
                    || stack.getItem() == Items.SOUL_TORCH) {
                return i;
            }
        }
        return -1;
    }
}
