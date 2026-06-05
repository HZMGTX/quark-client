package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import net.minecraft.client.gui.DrawContext;

public class SprintIndicator extends Module {
    private final IntSetting x = register(new IntSetting("X", "HUD X", 10, 0, 1920));
    private final IntSetting y = register(new IntSetting("Y", "HUD Y", 200, 0, 1080));

    public SprintIndicator() {
        super("Sprint Indicator", "Shows sprint and sneak status on HUD", Category.RENDER, 0);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null) return;
        DrawContext ctx = event.getDrawContext();
        String sprint = mc.player.isSprinting() ? "§aSprinting" : "§7Walking";
        String sneak = mc.player.isSneaking() ? " §eSneaking" : "";
        ctx.drawText(mc.textRenderer, sprint + sneak, x.get(), y.get(), 0xFFFFFF, true);
    }
}
