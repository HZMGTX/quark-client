package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.util.ColorUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;

public class HotbarViewer extends Module {
    private final BoolSetting showNames = register(new BoolSetting("Names", "Show item names below hotbar", true));
    private final BoolSetting showCount = register(new BoolSetting("Count", "Show item counts", true));

    public HotbarViewer() { super("HotbarViewer", "Enhanced hotbar with item names display", Category.RENDER); }
    @Override public void onEnable() { mc.getEventBus().subscribe(this); }
    @Override public void onDisable() { mc.getEventBus().unsubscribe(this); }

    @EventHandler
    public void onRender2D(EventRender2D e) {
        if (mc.player == null) return;
        DrawContext ctx = e.getDrawContext();
        int sw = ctx.getScaledWindowWidth(), sh = ctx.getScaledWindowHeight();
        int slotSize = 18, startX = sw / 2 - 81;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty()) continue;
            int slotX = startX + i * slotSize;
            if (showNames.isEnabled() && i == mc.player.getInventory().selectedSlot) {
                String name = stack.getName().getString();
                cc.quark.util.RenderUtil.drawCustomText(ctx, name, sw / 2 - mc.textRenderer.getWidth(name) / 2, sh - 59, 0xFFFFFFFF);
            }
            if (showCount.isEnabled() && stack.getCount() > 1) {
                cc.quark.util.RenderUtil.drawCustomText(ctx, String.valueOf(stack.getCount()), slotX + 11, sh - 33, 0xFFFFFFFF);
            }
        }
    }
}
