package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.BossBarHud;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.UUID;

public class BossBarInfo extends Module {
    private final BoolSetting showPercent = register(new BoolSetting("Percent", "Show boss HP percentage", true));

    public BossBarInfo() { super("BossBarInfo", "Shows extra boss bar info and HP percentage", Category.MISC); }
    @Override public void onEnable() { mc.getEventBus().subscribe(this); }
    @Override public void onDisable() { mc.getEventBus().unsubscribe(this); }

    @EventHandler
    public void onRender2D(EventRender2D e) {
        if (mc.player == null || !showPercent.isEnabled()) return;
        DrawContext ctx = e.getDrawContext();
        int y = 18;
        if (mc.inGameHud == null) return;
        BossBarHud bossBarHud = mc.inGameHud.getBossBarHud();
        if (bossBarHud == null) return;
        try {
            Field field = BossBarHud.class.getDeclaredField("bossBars");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<UUID, ?> bars = (Map<UUID, ?>) field.get(bossBarHud);
            for (var entry : bars.entrySet()) {
                float pct = 0f;
                try {
                    var getPercent = entry.getValue().getClass().getDeclaredMethod("getPercent");
                    getPercent.setAccessible(true);
                    pct = (float) getPercent.invoke(entry.getValue()) * 100f;
                } catch (Exception ignored) {}
                cc.quark.util.RenderUtil.drawCustomText(ctx, String.format("%.1f%%", pct), 2, y, 0xFFFF5555);
                y += 12;
            }
        } catch (Exception ignored) {}
    }
}
