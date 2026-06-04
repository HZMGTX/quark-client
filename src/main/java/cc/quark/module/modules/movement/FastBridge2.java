package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPreMotion;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.item.BlockItem;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class FastBridge2 extends Module {

    private final BoolSetting godBridge = register(new BoolSetting(
            "God Bridge", "Enable god-bridging (pitch-based placement)", false));

    private final BoolSetting teleBridge = register(new BoolSetting(
            "Tele Bridge", "Enable tele-bridging (instant placement without looking down)", false));

    private final DoubleSetting speed = register(new DoubleSetting(
            "Speed", "Bridging speed multiplier", 1.0, 0.5, 3.0));

    private boolean shouldSneak = false;

    public FastBridge2() {
        super("FastBridge2", "Enhanced fast bridge with timing assist", Category.MOVEMENT);
    }

    @Override
    public void onDisable() {
        shouldSneak = false;
        if (mc.options != null) {
            mc.options.sneakKey.setPressed(false);
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        // Only active when holding a block
        if (!(mc.player.getMainHandStack().getItem() instanceof BlockItem) &&
            !(mc.player.getOffHandStack().getItem() instanceof BlockItem)) {
            shouldSneak = false;
            mc.options.sneakKey.setPressed(false);
            return;
        }

        boolean movingBack = mc.player.input.movementForward < 0 ||
                             mc.player.input.movementSideways != 0;

        if (godBridge.isEnabled()) {
            // God-bridge: auto-sneak at edge for precise placement
            if (mc.player.isOnGround()) {
                // Check if player is at edge of block
                double edgeX = mc.player.getX() - Math.floor(mc.player.getX());
                double edgeZ = mc.player.getZ() - Math.floor(mc.player.getZ());
                boolean atEdge = edgeX < 0.3 || edgeX > 0.7 || edgeZ < 0.3 || edgeZ > 0.7;

                shouldSneak = atEdge;
                mc.options.sneakKey.setPressed(atEdge);

                if (atEdge) {
                    // Auto place block
                    if (mc.crosshairTarget instanceof BlockHitResult bhr) {
                        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, bhr);
                    }
                }
            }
        } else if (teleBridge.isEnabled()) {
            // Tele-bridge: place block below without looking down
            if (mc.player.isOnGround()) {
                Vec3d pos = mc.player.getPos();
                // Place block at feet level - 1 in look direction
                float yaw = mc.player.getYaw();
                double yawRad = Math.toRadians(yaw);
                Vec3d placeVec = pos.add(-Math.sin(yawRad) * -0.5, -1, Math.cos(yawRad) * -0.5);

                net.minecraft.util.math.BlockPos placePos = net.minecraft.util.math.BlockPos.ofFloored(placeVec);
                BlockHitResult hit = new BlockHitResult(placeVec, Direction.UP, placePos, false);
                mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
            }
        } else {
            // Regular fast-bridge: hold sneak when moving backward at edge
            if (mc.player.isOnGround() && movingBack) {
                shouldSneak = true;
                mc.options.sneakKey.setPressed(true);

                if (mc.crosshairTarget != null && mc.crosshairTarget.getType() == HitResult.Type.BLOCK) {
                    mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND,
                            (BlockHitResult) mc.crosshairTarget);
                }
            } else {
                shouldSneak = false;
                mc.options.sneakKey.setPressed(false);
            }
        }
    }

    @EventHandler
    public void onPreMotion(EventPreMotion event) {
        if (mc.player == null) return;
        // Adjust pitch for god-bridge
        if (godBridge.isEnabled() && shouldSneak) {
            event.setPitch(80.0f);
        }
    }
}
