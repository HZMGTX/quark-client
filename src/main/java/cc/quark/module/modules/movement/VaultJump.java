package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.block.FenceBlock;
import net.minecraft.block.WallBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class VaultJump extends Module {

    private final BoolSetting autoCrouch = register(new BoolSetting(
            "AutoCrouch", "Automatically crouch after vaulting to land safely", true));

    private boolean vaulting = false;

    public VaultJump() {
        super("VaultJump", "Vaults over fences and walls in one jump", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        vaulting = false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        if (vaulting) {
            if (mc.player.isOnGround()) {
                vaulting = false;
            }
            return;
        }

        if (!mc.player.isOnGround()) return;
        if (!mc.options.jumpKey.isPressed()) return;

        float yaw = mc.player.getYaw();
        double yawRad = Math.toRadians(yaw);
        double dirX = -Math.sin(yawRad);
        double dirZ = Math.cos(yawRad);

        Vec3d pos = mc.player.getPos();
        BlockPos frontPos = new BlockPos(
                (int) Math.floor(pos.x + dirX * 0.7),
                (int) Math.floor(pos.y),
                (int) Math.floor(pos.z + dirZ * 0.7)
        );

        var frontBlock = mc.world.getBlockState(frontPos).getBlock();
        boolean isFenceOrWall = frontBlock instanceof FenceBlock || frontBlock instanceof WallBlock;

        if (!isFenceOrWall) return;

        // Vault: jump with extra height to clear the fence/wall
        mc.player.jump();
        Vec3d vel = mc.player.getVelocity();
        mc.player.setVelocity(
                vel.x + dirX * 0.25,
                0.6,
                vel.z + dirZ * 0.25
        );
        vaulting = true;

        if (autoCrouch.isEnabled()) {
            // Initiate sneak after vault to control landing
            mc.options.sneakKey.setPressed(true);
        }
    }
}
