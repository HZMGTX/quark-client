package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class CrystalCalc extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Search radius for crystal placement", 4.0, 2.0, 8.0));

    private final BoolSetting showHUD = register(new BoolSetting(
            "Show HUD", "Display best placement info on HUD", true));

    private BlockPos bestPos = null;
    private double bestDamage = 0.0;

    public CrystalCalc() {
        super("CrystalCalc", "Calculates optimal crystal placement positions", Category.COMBAT);
    }

    @Override
    public void onDisable() {
        bestPos = null;
        bestDamage = 0.0;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        bestPos = null;
        bestDamage = 0.0;

        // Find nearest enemy player
        LivingEntity target = null;
        double minDist = Double.MAX_VALUE;
        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player) continue;
            if (!(entity instanceof PlayerEntity)) continue;
            LivingEntity living = (LivingEntity) entity;
            if (living.isDead() || living.getHealth() <= 0f) continue;
            double dist = mc.player.distanceTo(entity);
            if (dist < minDist) {
                minDist = dist;
                target = living;
            }
        }

        if (target == null) return;

        double r = range.get();
        BlockPos playerPos = mc.player.getBlockPos();
        int ri = (int) Math.ceil(r);

        for (int dx = -ri; dx <= ri; dx++) {
            for (int dy = -1; dy <= 2; dy++) {
                for (int dz = -ri; dz <= ri; dz++) {
                    BlockPos pos = playerPos.add(dx, dy, dz);
                    if (mc.player.squaredDistanceTo(Vec3d.ofCenter(pos)) > r * r) continue;

                    // Placement requires obsidian or bedrock below
                    BlockPos below = pos.down();
                    net.minecraft.block.Block belowBlock = mc.world.getBlockState(below).getBlock();
                    if (belowBlock != Blocks.OBSIDIAN && belowBlock != Blocks.BEDROCK) continue;

                    // Placement spot must be air
                    if (!mc.world.getBlockState(pos).isAir()) continue;
                    if (!mc.world.getBlockState(pos.up()).isAir()) continue;

                    // Simple distance-based damage estimate
                    double distToTarget = Math.sqrt(target.squaredDistanceTo(Vec3d.ofCenter(pos)));
                    if (distToTarget > 6.0) continue;

                    // Rough explosion damage approximation: 12 * (1 - dist/6)
                    double dmg = 12.0 * (1.0 - distToTarget / 6.0);
                    // Self-damage penalty
                    double selfDist = mc.player.squaredDistanceTo(Vec3d.ofCenter(pos));
                    double selfDmg = 12.0 * (1.0 - Math.sqrt(selfDist) / 6.0);
                    double score = dmg - selfDmg * 0.5;

                    if (score > bestDamage) {
                        bestDamage = score;
                        bestPos = pos;
                    }
                }
            }
        }
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (!showHUD.isEnabled()) return;
        if (mc.player == null) return;

        String text;
        if (bestPos != null) {
            text = String.format("Crystal: [%d,%d,%d] dmg=%.1f",
                    bestPos.getX(), bestPos.getY(), bestPos.getZ(), bestDamage);
        } else {
            text = "Crystal: No position found";
        }

        event.getDrawContext().drawText(
                mc.textRenderer, text, 4, 60, 0xFFFFFF00, true);
    }

    public BlockPos getBestPos() { return bestPos; }
}
