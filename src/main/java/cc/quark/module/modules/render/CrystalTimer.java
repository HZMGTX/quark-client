package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.event.events.EventRender3D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.Map;

public class CrystalTimer extends Module {

    private final IntSetting warningMs = register(new IntSetting("WarningMs", "Highlight in red when explosion is this many ms away", 500, 50, 2000));

    // Maps crystal entity ID → time it was last seen (ms). Crystals explode on the tick they are removed.
    private final Map<Integer, Long> firstSeen = new HashMap<>();

    public CrystalTimer() {
        super("CrystalTimer", "Shows a timer above end crystals indicating time since spawn", Category.RENDER);
    }

    @Override
    public void onDisable() {
        firstSeen.clear();
    }

    @EventHandler
    public void onRender3D(EventRender3D event) {
        if (mc.world == null || mc.player == null) return;
        long now = System.currentTimeMillis();
        for (Entity e : mc.world.getEntities()) {
            if (!(e instanceof EndCrystalEntity)) continue;
            firstSeen.putIfAbsent(e.getId(), now);
        }
        // Prune stale entries – crystal was removed (exploded)
        firstSeen.entrySet().removeIf(entry -> mc.world.getEntityById(entry.getKey()) == null);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.world == null || mc.player == null) return;
        DrawContext ctx = event.getDrawContext();
        long now = System.currentTimeMillis();
        int warn = warningMs.get();

        for (Entity e : mc.world.getEntities()) {
            if (!(e instanceof EndCrystalEntity)) continue;
            Long spawnTime = firstSeen.get(e.getId());
            if (spawnTime == null) continue;

            long elapsed = now - spawnTime;
            double[] screen = RenderUtil.project(e.getPos().add(0, 2.5, 0));
            if (screen == null) continue;

            int color = elapsed < warn ? 0xFFFF3333 : 0xFFFFFFFF;
            String text = String.format("%.1fs", elapsed / 1000.0);
            int tw = mc.textRenderer.getWidth(text);
            RenderUtil.drawCustomText(ctx, text, (int) screen[0] - tw / 2, (int) screen[1], color);
        }
    }
}
