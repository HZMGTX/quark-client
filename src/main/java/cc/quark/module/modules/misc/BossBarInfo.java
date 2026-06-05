package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.boss.BossBar;

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
        if (mc.inGameHud != null) {
            for (var bossBar : mc.inGameHud.getBossBarHud().bossBars.values()) {
                float pct = bossBar.getPercent() * 100f;
                cc.quark.util.RenderUtil.drawCustomText(ctx, String.format("%.1f%%", pct), 2, y, 0xFFFF5555);
                y += 12;
            }
        }
    }
}
