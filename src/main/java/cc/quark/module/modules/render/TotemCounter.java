package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.Items;

public class TotemCounter extends Module {
    private final IntSetting x = register(new IntSetting("X", "HUD X position", 10, 0, 1920));
    private final IntSetting y = register(new IntSetting("Y", "HUD Y position", 10, 0, 1080));

    public TotemCounter() {
        super("Totem Counter", "Shows totem count on HUD", Category.RENDER, 0);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null) return;
        DrawContext ctx = event.getDrawContext();
        int count = 0;
        for (int i = 0; i < mc.player.getInventory().size(); i++) {
            if (mc.player.getInventory().getStack(i).isOf(Items.TOTEM_OF_UNDYING)) count++;
        }
        String text = "§6Totems: §f" + count;
        ctx.drawText(mc.textRenderer, text, x.get(), y.get(), 0xFFFFFF, true);
    }
}
