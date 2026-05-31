package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventChat;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class MessageESP extends Module {

    private final IntSetting duration = register(new IntSetting("Duration", "How long messages stay visible (ticks)", 100, 20, 400));

    private final Map<String, long[]> messages = new LinkedHashMap<>();

    public MessageESP() {
        super("MessageESP", "Shows recent chat messages above the sender's head in the world", Category.RENDER);
    }

    @Override
    public void onEnable() {
        messages.clear();
    }

    @EventHandler
    public void onChat(EventChat event) {
        if (!event.isIncoming()) return;
        String msg = event.getMessage();
        int bracket = msg.indexOf('>');
        if (bracket < 1) return;
        String name = msg.substring(0, bracket).trim().replaceAll("§.", "");
        String text = msg.substring(bracket + 1).trim();
        messages.put(name, new long[]{System.currentTimeMillis(), 0});
        messages.put(name + "\0text", new long[]{System.currentTimeMillis(), text.hashCode()});
    }

    private final Map<String, String> lastText = new LinkedHashMap<>();

    @EventHandler
    public void onChat2(EventChat event) {
        if (!event.isIncoming()) return;
        String msg = event.getMessage().replaceAll("§.", "");
        int bracket = msg.indexOf('>');
        if (bracket < 1) return;
        String name = msg.substring(0, bracket).trim();
        String text = msg.substring(bracket + 1).trim();
        lastText.put(name, text);
        messages.put(name, new long[]{System.currentTimeMillis()});
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.world == null || mc.player == null) return;
        DrawContext ctx = event.getDrawContext();
        long now = System.currentTimeMillis();
        long dur = duration.get() * 50L;

        Iterator<Map.Entry<String, long[]>> it = messages.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, long[]> entry = it.next();
            if (now - entry.getValue()[0] > dur) { it.remove(); lastText.remove(entry.getKey()); }
        }

        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof PlayerEntity player)) continue;
            if (entity == mc.player) continue;
            String name = player.getGameProfile().getName();
            String text = lastText.get(name);
            if (text == null) continue;
            long[] ts = messages.get(name);
            if (ts == null || now - ts[0] > dur) continue;

            Vec3d pos = new Vec3d(entity.getX(), entity.getY() + entity.getHeight() + 0.6, entity.getZ());
            double[] screen = RenderUtil.project(pos);
            if (screen == null) continue;
            int tx = (int) screen[0] - mc.textRenderer.getWidth(text) / 2;
            ctx.drawTextWithShadow(mc.textRenderer, text, tx, (int) screen[1], 0xFFFFFFFF);
        }
    }
}
