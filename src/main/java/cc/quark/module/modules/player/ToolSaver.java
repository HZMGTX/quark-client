package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import net.minecraft.item.ItemStack;

/**
 * ToolSaver - stops attacking when the held tool is nearly broken.
 */
public class ToolSaver extends Module {

    private final IntSetting threshold = register(new IntSetting("Threshold", "Stop below this durability", 10, 1, 100));

    public ToolSaver() {
        super("ToolSaver", "Prevents breaking your tools", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        ItemStack held = mc.player.getMainHandStack();
        if (held.isEmpty() || !held.isDamageable()) return;
        int left = held.getMaxDamage() - held.getDamage();
        if (left <= threshold.get()) {
            mc.options.attackKey.setPressed(false);
        }
    }
}
