package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.ChatUtil;
import cc.quark.util.TimerUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class AutoArrow extends Module {

    private final BoolSetting autoCraft = register(new BoolSetting(
            "AutoCraft", "Attempt to craft arrows from feathers, flint, and sticks", true));

    private final IntSetting minArrows = register(new IntSetting(
            "Min Arrows", "Minimum arrow count before alerting", 16, 1, 64));

    private final TimerUtil timer = new TimerUtil();

    public AutoArrow() {
        super("AutoArrow", "Manages arrow inventory; crafts arrows from feathers/flint/sticks if available", Category.COMBAT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        if (!timer.hasReached(2000)) return;

        int arrowCount = countItem(Items.ARROW);

        if (arrowCount < minArrows.get()) {
            if (autoCraft.isEnabled()) {
                tryCraftArrows();
            } else {
                ChatUtil.warn("[AutoArrow] Low on arrows: " + arrowCount);
            }
        }

        timer.reset();
    }

    private void tryCraftArrows() {
        int feathers = countItem(Items.FEATHER);
        int flint = countItem(Items.FLINT);
        int sticks = countItem(Items.STICK);

        if (feathers < 1 || flint < 1 || sticks < 1) return;

        int batches = Math.min(Math.min(feathers, flint), sticks);
        if (batches <= 0) return;

        ChatUtil.warn("[AutoArrow] Would craft " + batches * 4 + " arrows (open crafting table to craft).");
    }

    private int countItem(net.minecraft.item.Item item) {
        if (mc.player == null) return 0;
        int count = 0;
        for (int i = 0; i < mc.player.getInventory().size(); i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isOf(item)) count += stack.getCount();
        }
        return count;
    }
}
