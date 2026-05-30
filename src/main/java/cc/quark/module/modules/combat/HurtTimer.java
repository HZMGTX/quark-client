package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventAttack;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.entity.LivingEntity;

/**
 * HurtTimer — only allows attacks when the target entity's hurtTime is 0,
 * preventing wasted hits during invincibility frames.
 */
public class HurtTimer extends Module {

    private final BoolSetting strict     = register(new BoolSetting("Strict",     "Only attack when hurtTime == 0",        true));
    private final IntSetting  extraDelay = register(new IntSetting ("ExtraDelay", "Extra ticks to wait after hurtTime=0",   0, 0, 5));

    private int waitTicks = 0;

    public HurtTimer() {
        super("HurtTimer", "Cancels attacks during target invincibility frames", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        waitTicks = 0;
    }

    @EventHandler
    public void onAttack(EventAttack event) {
        if (!strict.isEnabled()) return;
        if (!(event.getTarget() instanceof LivingEntity living)) return;

        if (living.hurtTime > 0) {
            event.cancel();
            waitTicks = extraDelay.get();
            return;
        }

        if (waitTicks > 0) {
            waitTicks--;
            event.cancel();
        }
    }
}
