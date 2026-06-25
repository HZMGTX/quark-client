package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class ArrowCounter extends Module {

    private final IntSetting x = register(new IntSetting("X", "HUD X position", 4, 0, 1920));
    private final IntSetting y = register(new IntSetting("Y", "HUD Y position", 60, 0, 1080));

    public ArrowCounter() {
        super("ArrowCounter", "Counts arrows in inventory on HUD", Category.RENDER);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null) return;
        DrawContext ctx = event.getDrawContext();

        int arrowCount = 0;
        for (int i = 0; i < mc.player.getInventory().size(); i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (!stack.isEmpty() && stack.getItem() instanceof ArrowItem) {
                arrowCount += stack.getCount();
            }
        }

        // Draw arrow item icon
        ItemStack arrowStack = new ItemStack(Items.ARROW);
        ctx.drawItem(arrowStack, x.get(), y.get());

        // Draw count text
        String countStr = "x" + arrowCount;
        int textColor = arrowCount == 0 ? 0xFFFF5555 : (arrowCount < 16 ? 0xFFFFAA00 : 0xFFFFFFFF);
        ctx.drawTextWithShadow(mc.textRenderer, countStr, x.get() + 18, y.get() + 4, textColor);
    }
}
