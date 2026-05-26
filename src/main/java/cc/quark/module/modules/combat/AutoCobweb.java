package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

/**
 * AutoCobweb - places a cobweb under the player when health drops low.
 */
public class AutoCobweb extends Module {

    private final IntSetting health = register(new IntSetting("Health", "Trigger health", 6, 1, 20));

    public AutoCobweb() {
        super("AutoCobweb", "Places a cobweb when low on health", Category.COMBAT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (mc.player.getHealth() > health.get()) return;
        if (!mc.player.getMainHandStack().isOf(Items.COBWEB)) return;
        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        mc.player.swingHand(Hand.MAIN_HAND);
    }
}
