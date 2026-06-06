package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.item.Items;

public class ArrowCounter2 extends Module {
    private final BoolSetting showWarning = register(new BoolSetting("ShowWarning", "Warn when arrows are low", true));
    public ArrowCounter2() { super("ArrowCounter2", "Counts arrows and shows HUD warning when low", Category.PLAYER); }
    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null) return;
        int count = 0;
        for (int i = 0; i < mc.player.getInventory().size(); i++) {
            var stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() == Items.ARROW || stack.getItem() == Items.SPECTRAL_ARROW
                    || stack.getItem() == Items.TIPPED_ARROW) {
                count += stack.getCount();
            }
        }
        int x = event.getDrawContext().getScaledWindowWidth() / 2 + 5;
        int y = event.getDrawContext().getScaledWindowHeight() - 40;
        int color = count < 16 ? 0xFFFF4444 : 0xFFFFFFFF;
        event.getDrawContext().drawText(mc.textRenderer, "Arrows: " + count, x, y, color, true);
    }
}
