package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.ColorUtil;
import net.minecraft.client.gui.DrawContext;

public class AttackCooldown extends Module {
    private final BoolSetting showPercent = register(new BoolSetting("Percent", "Show percentage number", true));
    private final IntSetting x = register(new IntSetting("X", "X position", 10, 0, 500));
    private final IntSetting y = register(new IntSetting("Y", "Y position", 100, 0, 500));

    public AttackCooldown() { super("AttackCooldown", "Displays attack cooldown bar on HUD", Category.COMBAT); }

    @EventHandler
    public void onRender2D(EventRender2D e) {
        if (mc.player == null) return;
        DrawContext ctx = e.getDrawContext();
        float progress = mc.player.getAttackCooldownProgress(0f);
        int barW = 60;
        int barH = 4;
        int px = x.get();
        int py = y.get();
        ctx.fill(px, py, px + barW, py + barH, ColorUtil.withAlpha(0x333333, 200));
        int fillW = (int)(barW * progress);
        int color = progress >= 1f ? 0x55FF55 : (progress > 0.5f ? 0xFFFF55 : 0xFF5555);
        ctx.fill(px, py, px + fillW, py + barH, ColorUtil.withAlpha(color, 220));
        if (showPercent.isEnabled()) {
            cc.quark.util.RenderUtil.drawCustomText(ctx, (int)(progress * 100) + "%", px + barW + 3, py - 1, 0xFFFFFFFF);
        }
    }
}
