package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class PearlPhase extends Module {

    private final BoolSetting autoThrow = register(new BoolSetting(
            "AutoThrow", "Automatically throw pearl when adjacent to a thin wall", true));

    public PearlPhase() {
        super("PearlPhase", "Throws ender pearl through thin walls by predicting phase position",
                Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        if (!autoThrow.isEnabled()) return;

        if (!playerHasPearl()) return;

        // Check for thin wall (1-block thick) in movement direction
        float yaw = mc.player.getYaw();
        double yawRad = Math.toRadians(yaw);
        double dirX = -Math.sin(yawRad);
        double dirZ = Math.cos(yawRad);

        BlockPos playerPos = mc.player.getBlockPos();
        BlockPos wallPos = playerPos.offset(dirX > 0 ? Direction.EAST : Direction.WEST)
                .offset(dirZ > 0 ? Direction.SOUTH : Direction.NORTH);

        // One block thick wall: wall block solid, block beyond it is air
        BlockPos beyondPos = wallPos.offset(dirX > 0 ? Direction.EAST : Direction.WEST)
                .offset(dirZ > 0 ? Direction.SOUTH : Direction.NORTH);

        boolean wallSolid = !mc.world.getBlockState(wallPos).isAir();
        boolean beyondAir = mc.world.getBlockState(beyondPos).isAir();
        boolean beyondAirUp = mc.world.getBlockState(beyondPos.up()).isAir();

        if (!wallSolid || !beyondAir || !beyondAirUp) return;

        // Aim slightly above the landing spot beyond the wall
        Vec3d target = new Vec3d(
                beyondPos.getX() + 0.5,
                beyondPos.getY() + 1.0,
                beyondPos.getZ() + 0.5
        );

        double dX = target.x - mc.player.getX();
        double dZ = target.z - mc.player.getZ();
        float throwYaw = (float) Math.toDegrees(Math.atan2(dZ, dX)) - 90;
        mc.player.setYaw(throwYaw);
        mc.player.setPitch(-20f);

        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
    }

    private boolean playerHasPearl() {
        for (int i = 0; i < mc.player.getInventory().size(); i++) {
            var stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() == Items.ENDER_PEARL) return true;
        }
        return false;
    }
}
