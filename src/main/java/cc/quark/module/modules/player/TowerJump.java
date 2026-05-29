package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.item.BlockItem;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class TowerJump extends Module {

    private final DoubleSetting speed = register(new DoubleSetting(
            "Speed", "Jump Y velocity multiplier", 1.0, 1.0, 3.0));
    private final BoolSetting placeBlock = register(new BoolSetting(
            "Place Block", "Place a block below while jumping upward", true));

    private boolean wasOnGround = false;

    public TowerJump() {
        super("TowerJump", "Rapidly jumps upward and optionally places blocks below to build a tower", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        wasOnGround = false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        if (!mc.options.jumpKey.isPressed()) return;

        boolean onGround = mc.player.isOnGround();

        if (onGround) {
            Vec3d vel = mc.player.getVelocity();
            mc.player.setVelocity(vel.x, 0.42 * speed.get(), vel.z);
            mc.player.jump();
        } else if (wasOnGround) {
            // Just left ground - place block if holding blocks
            if (placeBlock.isEnabled()
                    && mc.player.getMainHandStack().getItem() instanceof BlockItem
                    && mc.interactionManager != null) {
                placeBlockBelow();
            }
        }

        wasOnGround = onGround;
    }

    private void placeBlockBelow() {
        BlockPos below = mc.player.getBlockPos().down();
        if (!mc.world.getBlockState(below).isAir()) return;

        for (Direction dir : Direction.values()) {
            if (dir == Direction.UP) continue;
            BlockPos adj = below.offset(dir);
            if (mc.world.getBlockState(adj).isAir()) continue;

            Vec3d hitVec = Vec3d.ofCenter(adj).add(
                    dir.getOpposite().getOffsetX() * 0.5,
                    dir.getOpposite().getOffsetY() * 0.5,
                    dir.getOpposite().getOffsetZ() * 0.5);

            BlockHitResult hit = new BlockHitResult(hitVec, dir.getOpposite(), adj, false);
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
            return;
        }

        // Fallback: place on the block directly below
        BlockPos solidBelow = mc.player.getBlockPos().down(2);
        if (!mc.world.getBlockState(solidBelow).isAir()) {
            BlockHitResult hit = new BlockHitResult(
                    Vec3d.ofCenter(solidBelow).add(0, 0.5, 0),
                    Direction.UP, solidBelow, false);
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
        }
    }
}
