package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class PotionSelector extends Module {
    private final DoubleSetting healthThreshold = register(new DoubleSetting("HealthThreshold", "Health to auto-pot at", 14.0, 5.0, 20.0));
    private final BoolSetting preferSplash = register(new BoolSetting("PreferSplash", "Prefer splash potions", true));
    private int delay = 0;
    public PotionSelector() { super("PotionSelector", "Auto-selects best potion for the situation", Category.PLAYER); }
    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || ++delay < 5) return;
        delay = 0;
        if (mc.player.getHealth() > healthThreshold.getValue()) return;
        for (int i = 0; i < 9; i++) {
            ItemStack s = mc.player.getInventory().getStack(i);
            if (s.getItem() == Items.SPLASH_POTION || s.getItem() == Items.POTION) {
                mc.player.getInventory().selectedSlot = i;
                break;
            }
        }
    }
}
