package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPacketReceive;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.DeathMessageS2CPacket;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PopupCounter2 extends Module {

    // Tracks totem pops per player UUID this session
    private final Map<String, Integer> popCounts = new HashMap<>();

    public PopupCounter2() {
        super("PopupCounter2", "Shows totem pop count above each player's nameplate", Category.RENDER);
    }

    @Override
    public void onDisable() {
        popCounts.clear();
    }

    @EventHandler
    public void onPacket(EventPacketReceive event) {
        if (!(event.getPacket() instanceof DeathMessageS2CPacket pkt)) return;
        // The death message contains the player name; we match it against visible players.
        // Use mc.execute to mutate map safely on game thread.
        String msg = pkt.message().getString();
        mc.execute(() -> {
            if (mc.world == null) return;
            for (Entity e : mc.world.getEntities()) {
                if (!(e instanceof PlayerEntity p)) continue;
                if (p == mc.player) continue;
                // Heuristic: message contains the player's display name
                if (msg.contains(p.getDisplayName().getString())) {
                    String key = p.getUuidAsString();
                    popCounts.merge(key, 1, Integer::sum);
                }
            }
        });
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.world == null || mc.player == null) return;
        DrawContext ctx = event.getDrawContext();

        for (Entity e : mc.world.getEntities()) {
            if (!(e instanceof PlayerEntity p)) continue;
            if (p == mc.player) continue;

            Integer pops = popCounts.get(p.getUuidAsString());
            if (pops == null || pops == 0) continue;

            Vec3d headPos = new Vec3d(p.getX(), p.getY() + p.getHeight() + 0.6, p.getZ());
            double[] screen = RenderUtil.project(headPos);
            if (screen == null) continue;

            String text = "Pops: " + pops;
            int tw = mc.textRenderer.getWidth(text);
            RenderUtil.drawCustomText(ctx, text, (int) screen[0] - tw / 2, (int) screen[1], 0xFFFF4444);
        }
    }
}
