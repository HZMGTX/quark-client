package cc.quark.module.modules.player;

import cc.quark.Quark;
import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.item.Items;
import net.minecraft.item.ShieldItem;
import net.minecraft.util.Hand;

public class AutoShield extends Module {

    private final BoolSetting onDamage = register(new BoolSetting("OnDamage", "OnDamage", true));
    private final IntSetting health = register(new IntSetting("HealthThreshold", "HealthThreshold", 14, 2, 18));

    public AutoShield() {
        super("AutoShield", "Automatically blocks with a shield when attacked or low health", Category.PLAYER);
    }


    @EventHandler
    public void onTick(EventTick event) {
        
        if (mc == null || mc.player == null || mc.interactionManager == null) return;

        boolean hasShield = mc.player.getOffHandStack().getItem() instanceof ShieldItem
                         || mc.player.getMainHandStack().getItem() instanceof ShieldItem;
        if (!hasShield) return;

        boolean shouldBlock = mc.player.getHealth() <= health.get();

        if (shouldBlock) {
            Hand hand = mc.player.getOffHandStack().getItem() instanceof ShieldItem
                ? Hand.OFF_HAND : Hand.MAIN_HAND;
            mc.options.useKey.setPressed(true);
        } else {
            mc.options.useKey.setPressed(false);
        }
    }
}
