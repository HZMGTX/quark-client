package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.HitResult;

/**
 * FastInteract - removes the delay between block/entity interactions.
 */
public class FastInteract extends Module {

    public FastInteract() {
        super("FastInteract", "Removes the interaction cooldown", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.interactionManager == null || mc.crosshairTarget == null) return;
        if (mc.options.useKey.isPressed() && mc.crosshairTarget.getType() == HitResult.Type.BLOCK) {
            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        }
    }
}
