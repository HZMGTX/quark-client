package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.ColorUtil;
import net.minecraft.client.gui.DrawContext;

public class HealthBar extends Module {
    private final IntSetting x = register(new IntSetting("X", "X position", 2, 0, 1000));
    private final IntSetting y = register(new IntSetting("Y", "Y position", 30, 0, 600));
    private final BoolSetting showNum = register(new BoolSetting("Number", "Show HP number", true));

    public HealthBar() { super("HealthBar", "Custom health bar with color gradient", Category.RENDER); }
    @Override public void onEnable() { mc.getEventBus().subscribe(this); }
    @Override public void onDisable() { mc.getEventBus().unsubscribe(this); }

    @EventHandler
    public void onRender2D(EventRender2D e) {
        if (mc.player == null) return;
        DrawContext ctx = e.getDrawContext();
        float hp = mc.player.getHealth();
        float maxHp = mc.player.getMaxHealth();
        float pct = hp / maxHp;
        int px = x.get(), py = y.get(), bw = 80, bh = 6;
        ctx.fill(px, py, px + bw, py + bh, ColorUtil.withAlpha(0x222222, 200));
        int hpColor = pct > 0.5f ? 0x55FF55 : (pct > 0.25f ? 0xFFFF55 : 0xFF5555);
        ctx.fill(px, py, px + (int)(bw * pct), py + bh, ColorUtil.withAlpha(hpColor, 220));
        if (showNum.isEnabled())
            cc.quark.util.RenderUtil.drawCustomText(ctx, (int)hp + "/" + (int)maxHp, px + bw + 3, py, 0xFFFFFFFF);
    }
}
