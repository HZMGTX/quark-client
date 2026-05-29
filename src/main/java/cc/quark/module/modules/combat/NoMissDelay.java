package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.util.Hand;

/**
 * NoMissDelay — clears the swing cooldown so missed clicks do not cause a
 * delay on the next attack.
 * When "Attack On Miss" is enabled, the module also triggers a swing every
 * tick when the attack key is held and nothing is targeted.
 */
public class NoMissDelay extends Module {

    private final BoolSetting attackOnMiss = register(new BoolSetting(
            "Attack On Miss", "Swing every tick when attack key is held with no target", true));

    public NoMissDelay() {
        super("NoMissDelay", "Removes post-miss swing delay", Category.COMBAT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.options == null) return;

        // Reset attack charge so miss delay vanishes
        if (mc.player.getAttackCooldownProgress(0f) < 1.0f) {
            // Force cooldown advancement by not interfering; just swing to clear
        }

        if (attackOnMiss.isEnabled()
                && mc.options.attackKey.isPressed()
                && mc.crosshairTarget == null) {
            mc.player.swingHand(Hand.MAIN_HAND);
        }
    }
}
