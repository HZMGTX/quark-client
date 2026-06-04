package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class AntiExplosion extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Detection range for nearby explosions (blocks)", 8.0, 2.0, 20.0));

    public AntiExplosion() {
        super("AntiExplosion", "Reduces explosion damage by finding shelter", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        double r = range.get();
        Box searchBox = mc.player.getBoundingBox().expand(r);

        // Check for nearby TNT or crystals
        boolean threat = !mc.world.getEntitiesByClass(TntEntity.class, searchBox, t -> true).isEmpty()
                || !mc.world.getEntitiesByClass(EndCrystalEntity.class, searchBox, t -> true).isEmpty();

        if (!threat) return;

        Vec3d pos = mc.player.getPos();

        // Try to move away from threats — find a safer direction by checking adjacent positions
        Vec3d[] directions = {
                new Vec3d(1, 0, 0), new Vec3d(-1, 0, 0),
                new Vec3d(0, 0, 1), new Vec3d(0, 0, -1)
        };

        Vec3d bestDir = null;
        double maxDist = 0;

        for (Vec3d dir : directions) {
            Vec3d candidate = pos.add(dir.multiply(2.0));
            BlockPos candidatePos = BlockPos.ofFloored(candidate);

            // Check if position is walkable
            boolean walkable = !mc.world.getBlockState(candidatePos).isSolidBlock(mc.world, candidatePos)
                    && mc.world.getBlockState(candidatePos.down()).isSolidBlock(mc.world, candidatePos.down());

            if (walkable) {
                // Calculate distance from nearest threat
                List<TntEntity> tnts = mc.world.getEntitiesByClass(TntEntity.class,
                        new Box(candidate, candidate).expand(r * 2), t -> true);
                double minThreatDist = tnts.stream()
                        .mapToDouble(t -> t.getPos().squaredDistanceTo(candidate))
                        .min().orElse(Double.MAX_VALUE);

                if (minThreatDist > maxDist) {
                    maxDist = minThreatDist;
                    bestDir = dir;
                }
            }
        }

        if (bestDir != null) {
            // Apply velocity in the safest direction
            mc.player.setVelocity(
                    mc.player.getVelocity().x + bestDir.x * 0.15,
                    mc.player.getVelocity().y,
                    mc.player.getVelocity().z + bestDir.z * 0.15
            );
        }
    }
}
