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

import java.util.LinkedHashMap;
import java.util.Map;

public class TotemCounter extends Module {

    // player-name → pop count this session
    private final Map<String, Integer> pops = new LinkedHashMap<>();

    public TotemCounter() {
        super("TotemCounter", "Counts and displays total totem pops per player this session", Category.RENDER);
    }

    @Override
    public void onDisable() {
        pops.clear();
    }

    @EventHandler
    public void onPacket(EventPacketReceive event) {
        if (!(event.getPacket() instanceof DeathMessageS2CPacket pkt)) return;
        String msg = pkt.message().getString();
        mc.execute(() -> {
            if (mc.world == null) return;
            for (Entity e : mc.world.getEntities()) {
                if (!(e instanceof PlayerEntity p)) continue;
                if (p == mc.player) continue;
                String name = p.getDisplayName().getString();
                if (msg.contains(name)) {
                    pops.merge(name, 1, Integer::sum);
                }
            }
        });
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null || pops.isEmpty()) return;
        DrawContext ctx = event.getDrawContext();
        int x = 4;
        int y = 4;
        RenderUtil.drawCustomText(ctx, "Totem Pops:", x, y, 0xFFFFDD00);
        y += 10;
        for (Map.Entry<String, Integer> entry : pops.entrySet()) {
            RenderUtil.drawCustomText(ctx, entry.getKey() + ": " + entry.getValue(), x, y, 0xFFFFFFFF);
            y += 10;
        }
    }
}
