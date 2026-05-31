package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class Rappel extends Module {

    private final DoubleSetting speed = register(new DoubleSetting(
            "Speed", "Descent speed while holding shift against a wall", 0.3, 0.05, 1.5));

    public Rappel() {
        super("Rappel", "Descends vertical surfaces quickly while holding shift", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        if (!mc.options.sneakKey.isPressed()) return;
        if (mc.player.isOnGround()) return;

        // Check for a solid wall adjacent to the player
        BlockPos pos = mc.player.getBlockPos();
        boolean wallFound = false;
        for (net.minecraft.util.math.Direction dir :
                new net.minecraft.util.math.Direction[]{
                        net.minecraft.util.math.Direction.NORTH,
                        net.minecraft.util.math.Direction.SOUTH,
                        net.minecraft.util.math.Direction.EAST,
                        net.minecraft.util.math.Direction.WEST}) {
            BlockPos adj = pos.offset(dir);
            if (!mc.world.getBlockState(adj).isAir()) {
                wallFound = true;
                break;
            }
        }

        if (!wallFound) return;

        Vec3d vel = mc.player.getVelocity();
        mc.player.setVelocity(vel.x * 0.5, -speed.get(), vel.z * 0.5);
        mc.player.fallDistance = 0;
    }
}
