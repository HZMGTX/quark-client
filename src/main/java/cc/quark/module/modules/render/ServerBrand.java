package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import net.minecraft.client.gui.DrawContext;

public class ServerBrand extends Module {

    private final IntSetting x = register(new IntSetting("X", "X pos", 4, 0, 500));
    private final IntSetting y = register(new IntSetting("Y", "Y pos", 44, 0, 500));

    public ServerBrand() {
        super("ServerBrand", "Shows the connected server address", Category.RENDER);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        DrawContext ctx = event.getDrawContext();
        String brand = "Singleplayer";
        if (mc.getCurrentServerEntry() != null) {
            brand = mc.getCurrentServerEntry().address;
        }
        ctx.drawTextWithShadow(mc.textRenderer, "Server: " + brand, x.get(), y.get(), 0xFFFFFFFF);
    }
}
