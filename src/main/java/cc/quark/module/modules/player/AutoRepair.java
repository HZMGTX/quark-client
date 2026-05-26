package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.util.ChatUtil;
import net.minecraft.item.ItemStack;

/**
 * AutoRepair - warns when the held tool is close to breaking so it can be swapped/repaired.
 */
public class AutoRepair extends Module {

    private final IntSetting threshold = register(new IntSetting("Threshold", "Warn at this remaining durability percent", 10, 1, 50));
    private boolean warned = false;

    public AutoRepair() {
        super("AutoRepair", "Warns when held tool durability is low", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        warned = false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        ItemStack stack = mc.player.getMainHandStack();
        if (stack.isEmpty() || !stack.isDamageable()) {
            warned = false;
            return;
        }
        int remaining = stack.getMaxDamage() - stack.getDamage();
        int percent = (int) (100.0 * remaining / stack.getMaxDamage());
        if (percent <= threshold.get()) {
            if (!warned) {
                ChatUtil.warn("Held item durability low (" + percent + "%)");
                warned = true;
            }
        } else {
            warned = false;
        }
    }
}
