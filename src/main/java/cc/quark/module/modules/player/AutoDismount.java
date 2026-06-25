package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.math.BlockPos;

public class AutoDismount extends Module {

    private final BoolSetting onLava     = register(new BoolSetting("On Lava",    "Dismount when over lava",           true));
    private final BoolSetting onVoid     = register(new BoolSetting("On Void",    "Dismount when over the void",       true));
    private final BoolSetting onFire     = register(new BoolSetting("On Fire",    "Dismount when vehicle is on fire",  true));
    private final BoolSetting onHighFall = register(new BoolSetting("High Fall",  "Dismount when falling from height", false));
    private final DoubleSetting fallHeight = register(new DoubleSetting("Fall Height", "Min Y drop to trigger High Fall dismount", 5.0, 1.0, 20.0));

    public AutoDismount() {
        super("AutoDismount", "Auto-dismounts from rides in dangerous situations", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        Entity vehicle = mc.player.getVehicle();
        if (vehicle == null) return;

        boolean shouldDismount = false;

        // Check block below vehicle
        BlockPos below = vehicle.getBlockPos().down();

        if (onLava.isEnabled()) {
            if (mc.world.getBlockState(below).getBlock() == Blocks.LAVA
                    || mc.world.getBlockState(below).getBlock() == Blocks.FLOWING_LAVA) {
                shouldDismount = true;
            }
        }

        if (onVoid.isEnabled()) {
            if (vehicle.getY() < (mc.world.getDimension().minY() + 5)) {
                shouldDismount = true;
            }
        }

        if (onFire.isEnabled() && vehicle.isOnFire()) {
            shouldDismount = true;
        }

        if (onHighFall.isEnabled() && !vehicle.isOnGround()) {
            double velY = vehicle.getVelocity().y;
            // Estimate fall distance: v^2 / (2g)
            if (velY < 0 && Math.abs(velY * velY / (2 * 0.08)) > fallHeight.get()) {
                shouldDismount = true;
            }
        }

        if (shouldDismount) {
            mc.player.stopRiding();
        }
    }
}
