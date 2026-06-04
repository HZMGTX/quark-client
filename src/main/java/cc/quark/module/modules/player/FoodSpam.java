package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

public class FoodSpam extends Module {

    private final IntSetting delay = register(new IntSetting(
            "Delay", "Milliseconds between food use attempts", 200, 50, 2000));

    private final TimerUtil timer = new TimerUtil();

    public FoodSpam() {
        super("FoodSpam", "Repeatedly uses food item for max saturation", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(delay.get())) return;

        ItemStack main = mc.player.getMainHandStack();
        ItemStack off = mc.player.getOffHandStack();

        Hand hand = null;
        if (main.getItem().getFoodComponent() != null) {
            hand = Hand.MAIN_HAND;
        } else if (off.getItem().getFoodComponent() != null) {
            hand = Hand.OFF_HAND;
        }

        if (hand == null) return;

        mc.interactionManager.interactItem(mc.player, hand);
        mc.options.useKey.setPressed(true);
        timer.reset();
    }

    @Override
    public void onDisable() {
        if (mc.options != null) {
            mc.options.useKey.setPressed(false);
        }
    }
}
