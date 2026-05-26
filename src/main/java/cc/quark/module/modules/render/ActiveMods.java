package cc.quark.module.modules.render;

import cc.quark.Quark;
import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import net.minecraft.client.gui.DrawContext;

public class ActiveMods extends Module {

    private final IntSetting x = register(new IntSetting("X", "X pos", 4, 0, 500));
    private final IntSetting y = register(new IntSetting("Y", "Y pos", 64, 0, 500));

    public ActiveMods() {
        super("ActiveMods", "Shows a count of currently enabled modules", Category.RENDER);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        DrawContext ctx = event.getDrawContext();
        int count = 0;
        for (Module module : Quark.getInstance().getModuleManager().getModules()) {
            if (module.isEnabled()) count++;
        }
        ctx.drawTextWithShadow(mc.textRenderer, "Active: " + count, x.get(), y.get(), 0xFF55FF55);
    }
}
