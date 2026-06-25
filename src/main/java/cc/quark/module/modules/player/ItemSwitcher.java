package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventKey;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.ModeSetting;
import net.minecraft.item.*;

public class ItemSwitcher extends Module {
    private final ModeSetting trigger = register(new ModeSetting("Trigger", "When to switch", "Attack", "Attack", "Use", "Manual"));

    public ItemSwitcher() { super("ItemSwitcher", "Automatically switches to best item for context", Category.PLAYER); }

    @EventHandler
    public void onKey(EventKey e) {
        if (mc.player == null) return;
        if (trigger.get().equals("Manual") && e.getKeyCode() == org.lwjgl.glfw.GLFW.GLFW_KEY_R) {
            switchToBestItem();
        }
    }

    private void switchToBestItem() {
        if (mc.player == null) return;
        for (int i = 0; i < 9; i++) {
            Item item = mc.player.getInventory().getStack(i).getItem();
            if (item instanceof SwordItem || item instanceof AxeItem) {
                mc.player.getInventory().selectedSlot = i;
                return;
            }
        }
    }
}
