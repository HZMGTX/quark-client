package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class CrystalTracker extends Module {

    private final IntSetting maxTrack = register(new IntSetting("MaxTrack", "Maximum number of crystals to track", 5, 1, 20));

    private static class TrackedCrystal {
        final EndCrystalEntity entity;
        long spawnTime;
        double predictedDmg;

        TrackedCrystal(EndCrystalEntity e) {
            entity = e;
            spawnTime = System.currentTimeMillis();
            predictedDmg = 0;
        }
    }

    private final List<TrackedCrystal> tracked = new ArrayList<>();

    public CrystalTracker() {
        super("CrystalTracker", "Tracks placed end crystals; shows timer and predicted damage for each", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        mc.execute(() -> tracked.clear());
    }

    @Override
    public void onDisable() {
        mc.execute(() -> tracked.clear());
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.world == null || mc.player == null) return;

        mc.execute(() -> {
            tracked.removeIf(tc -> tc.entity.isRemoved() || tc.entity.isDead());

            for (Entity e : mc.world.getEntities()) {
                if (!(e instanceof EndCrystalEntity crystal)) continue;
                boolean alreadyTracked = false;
                for (TrackedCrystal tc : tracked) {
                    if (tc.entity == crystal) { alreadyTracked = true; break; }
                }
                if (!alreadyTracked && tracked.size() < maxTrack.get()) {
                    tracked.add(new TrackedCrystal(crystal));
                }
            }

            for (TrackedCrystal tc : tracked) {
                PlayerEntity nearestTarget = findNearestEnemy();
                if (nearestTarget != null) {
                    tc.predictedDmg = calcDamage(nearestTarget, tc.entity.getPos().add(0, 1, 0));
                }
            }
        });
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (tracked.isEmpty()) return;
        DrawContext ctx = event.getDrawContext();
        int y = 40;
        ctx.drawText(mc.textRenderer, "Crystals:", 4, y, 0xFFFFFF00, true);
        y += 10;
        for (TrackedCrystal tc : tracked) {
            if (tc.entity.isRemoved()) continue;
            long age = (System.currentTimeMillis() - tc.spawnTime);
            String line = String.format("%.0fms dmg:%.1f", (double) age, tc.predictedDmg);
            ctx.drawText(mc.textRenderer, line, 4, y, 0xFFFF4444, true);
            y += 10;
        }
    }

    private PlayerEntity findNearestEnemy() {
        PlayerEntity nearest = null;
        double best = Double.MAX_VALUE;
        for (Entity e : mc.world.getEntities()) {
            if (!(e instanceof PlayerEntity p)) continue;
            if (p == mc.player || p.isDead() || p.getHealth() <= 0) continue;
            double d = mc.player.distanceTo(p);
            if (d < best) { best = d; nearest = p; }
        }
        return nearest;
    }

    private double calcDamage(PlayerEntity target, Vec3d explosionPos) {
        double dist = target.getPos().add(0, target.getHeight() / 2.0, 0).distanceTo(explosionPos);
        double radius = 6.0;
        if (dist > radius) return 0;
        double exposure = 1.0 - (dist / radius);
        double impact = exposure * (0.7 + radius * 0.3);
        return (impact * impact + impact) * 7.0 * radius + 1.0;
    }
}
