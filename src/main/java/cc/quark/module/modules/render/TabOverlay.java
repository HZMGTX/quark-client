package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.util.ColorUtil;
import net.minecraft.client.gui.DrawContext;

public class TabOverlay extends Module {
    private final BoolSetting customHeader = register(new BoolSetting("Custom Header", "Show custom tab header", true));

    public TabOverlay() { super("TabOverlay", "Custom styling for the player tab list", Category.RENDER); }
    @Override public void onEnable() { mc.getEventBus().subscribe(this); }
    @Override public void onDisable() { mc.getEventBus().unsubscribe(this); }

    @EventHandler
    public void onRender2D(EventRender2D e) {
        if (!mc.options.playerListKey.isPressed()) return;
        DrawContext ctx = e.getDrawContext();
        int sw = ctx.getScaledWindowWidth();
        if (customHeader.isEnabled()) {
            String header = "§6§lQuark.cc §7| §f" + cc.quark.Quark.VERSION;
            int hw = mc.textRenderer.getWidth(header);
            ctx.fill(sw/2 - hw/2 - 4, 2, sw/2 + hw/2 + 4, 14, ColorUtil.withAlpha(0x111111, 200));
            cc.quark.util.RenderUtil.drawCustomText(ctx, header, sw/2 - hw/2, 4, 0xFFFFFFFF);
        }
    }
}
