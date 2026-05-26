package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.util.ChatUtil;
import net.minecraft.item.Items;

/**
 * TotemPop - announces in chat when the player's totem of undying triggers.
 */
public class TotemPop extends Module {

    private boolean hadTotem;

    public TotemPop() {
        super("TotemPop", "Announces when your totem pops", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        hadTotem = false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        boolean hasTotem = mc.player.getOffHandStack().isOf(Items.TOTEM_OF_UNDYING)
                || mc.player.getMainHandStack().isOf(Items.TOTEM_OF_UNDYING);
        if (hadTotem && !hasTotem) {
            ChatUtil.warn("Totem popped!");
        }
        hadTotem = hasTotem;
    }
}
