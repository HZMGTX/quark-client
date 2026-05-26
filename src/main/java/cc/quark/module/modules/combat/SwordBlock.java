package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.item.SwordItem;
import net.minecraft.util.Hand;

/**
 * SwordBlock - holds use with a sword to emulate blocking.
 */
public class SwordBlock extends Module {

    private final BoolSetting onlySword = register(new BoolSetting("Only Sword", "Only block while holding a sword", true));

    public SwordBlock() {
        super("SwordBlock", "Blocks with the held sword", Category.COMBAT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.interactionManager == null) return;
        if (onlySword.isEnabled() && !(mc.player.getMainHandStack().getItem() instanceof SwordItem)) return;
        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
    }
}
