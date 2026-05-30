package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.setting.ModeSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class LiquidFiller extends Module {

    private final ModeSetting fluid = register(new ModeSetting(
            "Fluid", "Which fluid to place",
            "Water", "Water", "Lava"));
    private final IntSetting range = register(new IntSetting(
            "Range", "Placement range", 3, 1, 6));

    private final TimerUtil timer = new TimerUtil();

    public LiquidFiller() {
        super("LiquidFiller", "Places water or lava source blocks in a configurable area", Category.WORLD);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(200)) return;

        // Check player is holding the correct bucket
        var heldItem = mc.player.getMainHandStack().getItem();
        boolean holdingWater = heldItem == Items.WATER_BUCKET;
        boolean holdingLava = heldItem == Items.LAVA_BUCKET;

        String mode = fluid.get();
        if (mode.equals("Water") && !holdingWater) return;
        if (mode.equals("Lava") && !holdingLava) return;

        // Use crosshair target if within range
        HitResult hit = mc.crosshairTarget;
        if (hit == null || hit.getType() != HitResult.Type.BLOCK) return;

        BlockHitResult blockHit = (BlockHitResult) hit;
        BlockPos targetPos = blockHit.getBlockPos().offset(blockHit.getSide());

        if (!targetPos.isWithinDistance(mc.player.getPos(), range.get())) return;

        // Only place if target position is air
        if (!mc.world.getBlockState(targetPos).isAir()) return;

        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND,
                new BlockHitResult(Vec3d.ofCenter(blockHit.getBlockPos()), blockHit.getSide(), blockHit.getBlockPos(), false));
        mc.player.swingHand(Hand.MAIN_HAND);
        timer.reset();
    }
}
