package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.setting.StringSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class ItemCount extends Module {
    private final StringSetting itemId = register(new StringSetting("Item", "Item ID to count", "minecraft:golden_apple"));
    private final IntSetting x = register(new IntSetting("X", "HUD X", 10, 0, 1920));
    private final IntSetting y = register(new IntSetting("Y", "HUD Y", 40, 0, 1080));

    public ItemCount() {
        super("Item Count", "Shows count of specific item in inventory", Category.RENDER, 0);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null) return;
        DrawContext ctx = event.getDrawContext();
        try {
            Item item = Registries.ITEM.get(Identifier.of(itemId.get()));
            int count = 0;
            for (int i = 0; i < mc.player.getInventory().size(); i++) {
                var stack = mc.player.getInventory().getStack(i);
                if (stack.isOf(item)) count += stack.getCount();
            }
            ctx.drawText(mc.textRenderer, "§e" + itemId.get().split(":")[1] + ": §f" + count, x.get(), y.get(), 0xFFFFFF, true);
        } catch (Exception ignored) {}
    }
}
