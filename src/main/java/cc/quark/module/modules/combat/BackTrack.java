package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPreMotion;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BackTrack extends Module {

    private final IntSetting historyMs = register(new IntSetting(
            "History Ms", "How many milliseconds of position history to store", 500, 100, 2000));

    private final DoubleSetting predict = register(new DoubleSetting(
            "Predict", "Multiplier for velocity-based prediction", 0.3, 0.0, 2.0));

    private static class TimedPos {
        final Vec3d pos;
        final long time;
        TimedPos(Vec3d pos, long time) { this.pos = pos; this.time = time; }
    }

    private final Map<UUID, Deque<TimedPos>> history = new HashMap<>();
    private final Map<UUID, Vec3d> predictedPositions = new HashMap<>();

    public BackTrack() {
        super("BackTrack", "Tracks enemy position history for prediction", Category.COMBAT);
    }

    @Override
    public void onDisable() {
        history.clear();
        predictedPositions.clear();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        long now = System.currentTimeMillis();
        long maxAge = historyMs.get();

        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player) continue;
            if (!(entity instanceof PlayerEntity)) continue;

            UUID uuid = entity.getUuid();
            Deque<TimedPos> queue = history.computeIfAbsent(uuid, k -> new ArrayDeque<>());

            // Record current position
            queue.addLast(new TimedPos(entity.getPos(), now));

            // Prune old entries
            while (!queue.isEmpty() && now - queue.peekFirst().time > maxAge) {
                queue.pollFirst();
            }

            // Compute prediction: use last two positions to extrapolate velocity
            if (queue.size() >= 2) {
                TimedPos latest = queue.peekLast();
                TimedPos prev = null;
                for (TimedPos tp : queue) {
                    if (tp != latest) prev = tp;
                }
                if (prev != null) {
                    Vec3d vel = latest.pos.subtract(prev.pos);
                    double p = predict.get();
                    Vec3d predicted = latest.pos.add(vel.multiply(p));
                    predictedPositions.put(uuid, predicted);
                }
            }
        }
    }

    @EventHandler
    public void onPreMotion(EventPreMotion event) {
        // No packet spoofing needed here; prediction data is available for other modules
    }

    public Vec3d getPredictedPosition(UUID uuid) {
        return predictedPositions.get(uuid);
    }
}
