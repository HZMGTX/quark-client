package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventJump;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;

/**
 * HighJump2 - adds a flat extra upward velocity boost on top of the vanilla jump.
 * Unlike {@link HighJump} (which uses a multiplier), this module applies an
 * absolute boost, making it easier to dial in a specific jump height.
 */
public class HighJump2 extends Module {

    private final DoubleSetting boost = register(new DoubleSetting(
            "Boost", "Extra vertical velocity added on jump (blocks/tick)", 0.5, 0.1, 3.0));

    public HighJump2() {
        super("High Jump+", "Adds a flat upward velocity boost on jump", Category.MOVEMENT);
    }

    @Override
    public String getSuffix() {
        return String.format("+%.1f", boost.get());
    }

    @EventHandler
    public void onJump(EventJump event) {
        if (mc.player == null) return;
        mc.player.setVelocity(mc.player.getVelocity().add(0, boost.get(), 0));
    }
}
