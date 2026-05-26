package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import net.minecraft.client.gui.DrawContext;

public class SessionInfo extends Module {

    private final IntSetting x = register(new IntSetting("X", "X pos", 4, 0, 500));
    private final IntSetting y = register(new IntSetting("Y", "Y pos", 54, 0, 500));

    public SessionInfo() {
        super("SessionInfo", "Displays the current session username", Category.RENDER);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.getSession() == null) return;
        DrawContext ctx = event.getDrawContext();
        ctx.drawTextWithShadow(mc.textRenderer, "User: " + mc.getSession().getUsername(), x.get(), y.get(), 0xFFFFFFFF);
    }
}
