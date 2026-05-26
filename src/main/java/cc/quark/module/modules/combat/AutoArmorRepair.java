package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.util.ChatUtil;
import net.minecraft.item.ItemStack;

/**
 * AutoArmorRepair - warns when the held item count gets low (proxy for repair need).
 */
public class AutoArmorRepair extends Module {

    private final IntSetting threshold = register(new IntSetting("Threshold", "Item count to warn at", 4, 1, 64));

    private int ticks;

    public AutoArmorRepair() {
        super("AutoArmorRepair", "Warns when gear stack runs low", Category.COMBAT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        ticks++;
        if (ticks < 40) return;
        ticks = 0;

        ItemStack stack = mc.player.getMainHandStack();
        if (!stack.isEmpty() && stack.getCount() <= threshold.get()) {
            ChatUtil.warn("Gear low: " + stack.getCount() + " left");
        }
    }
}
