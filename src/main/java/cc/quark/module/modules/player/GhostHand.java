package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

public class GhostHand extends Module {

    private final DoubleSetting extraReach = register(new DoubleSetting(
            "Extra Reach", "Additional block interaction distance", 3.0, 0.5, 10.0));
    private final BoolSetting requireHeld = register(new BoolSetting(
            "Require Right-Click", "Only send packet while right-click is held", true));

    public GhostHand() {
        super("GhostHand", "Sends block-interact packets from facing direction to extend block interaction reach", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.getNetworkHandler() == null) return;

        if (requireHeld.isEnabled()) {
            long handle = mc.getWindow().getHandle();
            boolean rightHeld = GLFW.glfwGetMouseButton(handle, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;
            if (!rightHeld) return;
        }

        double reach = 4.5 + extraReach.get();
        Vec3d eyePos = mc.player.getEyePos();
        Vec3d lookVec = mc.player.getRotationVec(1.0f);
        Vec3d endPos = eyePos.add(lookVec.multiply(reach));

        BlockPos targetPos = BlockPos.ofFloored(endPos);
        if (mc.world.getBlockState(targetPos).isAir()) return;

        Direction side = getClosestFace(eyePos, targetPos);
        Vec3d hitVec = endPos.subtract(targetPos.getX(), targetPos.getY(), targetPos.getZ());

        BlockHitResult hitResult = new BlockHitResult(hitVec, side, targetPos, false);
        mc.getNetworkHandler().sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, hitResult, 0));
    }

    private Direction getClosestFace(Vec3d from, BlockPos pos) {
        double cx = pos.getX() + 0.5 - from.x;
        double cy = pos.getY() + 0.5 - from.y;
        double cz = pos.getZ() + 0.5 - from.z;

        double ax = Math.abs(cx), ay = Math.abs(cy), az = Math.abs(cz);

        if (ax >= ay && ax >= az) return cx < 0 ? Direction.EAST : Direction.WEST;
        if (ay >= ax && ay >= az) return cy < 0 ? Direction.UP   : Direction.DOWN;
        return cz < 0 ? Direction.SOUTH : Direction.NORTH;
    }
}
