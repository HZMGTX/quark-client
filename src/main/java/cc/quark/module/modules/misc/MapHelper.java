package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.Items;

public class MapHelper extends Module {
    private final BoolSetting autoZoom = register(new BoolSetting("Auto Zoom", "Auto-show map when holding it", true));
    private final IntSetting size = register(new IntSetting("Size", "Map preview size", 64, 32, 256));

    public MapHelper() { super("MapHelper", "Enhanced map item display and navigation", Category.MISC); }
    @Override public void onEnable() { mc.getEventBus().subscribe(this); }
    @Override public void onDisable() { mc.getEventBus().unsubscribe(this); }

    @EventHandler
    public void onRender2D(EventRender2D e) {
        if (mc.player == null || !autoZoom.isEnabled()) return;
        var held = mc.player.getMainHandStack();
        if (held.getItem() != Items.FILLED_MAP) return;
        DrawContext ctx = e.getDrawContext();
        int sw = ctx.getScaledWindowWidth();
        int sh = ctx.getScaledWindowHeight();
        int s = size.get();
        ctx.fill(sw/2 - s/2 - 2, sh/2 - s/2 - 2, sw/2 + s/2 + 2, sh/2 + s/2 + 2, 0xFF333333);
        cc.quark.util.RenderUtil.drawCustomText(ctx, "Map #" + FilledMapItem.getMapId(held), sw/2 - 20, sh/2 + s/2 + 4, 0xFFAAAAAA);
    }
}
