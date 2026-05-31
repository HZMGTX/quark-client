package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

import java.util.ArrayList;
import java.util.List;

public class PlayerCount extends Module {

    private final BoolSetting showNames = register(new BoolSetting("ShowNames", "List player names below the count", false));
    private final IntSetting posX = register(new IntSetting("X", "HUD X position", 4, 0, 3000));
    private final IntSetting posY = register(new IntSetting("Y", "HUD Y position", 4, 0, 3000));

    public PlayerCount() {
        super("PlayerCount", "Shows the number of players in render distance", Category.RENDER);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.world == null || mc.player == null) return;
        DrawContext ctx = event.getDrawContext();
        List<String> names = new ArrayList<>();

        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player) continue;
            if (!(entity instanceof PlayerEntity player)) continue;
            names.add(player.getGameProfile().getName());
        }

        int x = posX.get(), y = posY.get();
        int lh = mc.textRenderer.fontHeight + 2;
        ctx.drawTextWithShadow(mc.textRenderer, "Players: " + names.size(), x, y, 0xFF55AAFF);

        if (showNames.isEnabled()) {
            int dy = y + lh;
            for (String name : names) {
                ctx.drawTextWithShadow(mc.textRenderer, "  " + name, x, dy, 0xFFAADDFF);
                dy += lh;
            }
        }
    }
}
