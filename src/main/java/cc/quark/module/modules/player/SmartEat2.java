package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

public class SmartEat2 extends Module {
    private final DoubleSetting threshold = register(new DoubleSetting("Threshold", "Hunger to start eating", 18.0, 1.0, 20.0));
    private final BoolSetting eatGapple = register(new BoolSetting("EatGapple", "Eat golden apples on low health", true));
    private final DoubleSetting gappleHealth = register(new DoubleSetting("GappleHealth", "Health to eat golden apple", 12.0, 5.0, 19.0));
    public SmartEat2() { super("SmartEat2", "Smart eating with situational awareness", Category.PLAYER); }
    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        int hunger = mc.player.getHungerManager().getFoodLevel();
        float health = mc.player.getHealth();
        if (hunger < threshold.getValue() || (eatGapple.getValue() && health < gappleHealth.getValue())) {
            for (int i = 0; i < 9; i++) {
                ItemStack s = mc.player.getInventory().getStack(i);
                if (!s.isEmpty() && s.getItem().getFoodComponent() != null) {
                    mc.player.getInventory().selectedSlot = i;
                    mc.options.useKey.setPressed(true);
                    return;
                }
            }
        } else {
            mc.options.useKey.setPressed(false);
        }
    }
}
