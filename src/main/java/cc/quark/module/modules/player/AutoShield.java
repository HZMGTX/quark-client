package cc.quark.module.modules.player;

import cc.quark.Quark;
import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.module.setting.BoolSetting;
import cc.quark.module.setting.IntSetting;
import net.minecraft.item.Items;
import net.minecraft.item.ShieldItem;
import net.minecraft.util.Hand;

public class AutoShield extends Module {

    private final BoolSetting onDamage = new BoolSetting("OnDamage", true);
    private final IntSetting health = new IntSetting("HealthThreshold", 14, 2, 18);

    public AutoShield() {
        super("AutoShield", "Automatically blocks with a shield when attacked or low health", Category.PLAYER);
        addSettings(onDamage, health);
    }

    @Override public void onEnable()  { Quark.mc.getEventBus().subscribe(this); }
    @Override public void onDisable() { Quark.mc.getEventBus().unsubscribe(this); }

    @EventHandler
    public void onTick(EventTick event) {
        var mc = Quark.mc;
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
