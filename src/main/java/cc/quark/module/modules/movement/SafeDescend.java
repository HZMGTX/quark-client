package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.Vec3d;

/**
 * SafeDescend - allows the player to descend slowly and safely by holding the
 * sneak key while falling.  Fall damage is negated so long as this module is
 * active and the safe-descend conditions are met.
 *
 * <p>When {@code Auto Activate} is enabled the module automatically activates
 * whenever the player is falling (no key input required).
 */
public class SafeDescend extends Module {

    private final DoubleSetting descendSpeed = register(new DoubleSetting(
            "Descend Speed", "Downward velocity while safe-descending (blocks/tick)", 0.15, 0.02, 1.0));

    private final BoolSetting autoActivate = register(new BoolSetting(
            "Auto Activate", "Activate automatically when falling, no sneak needed", false));

    public SafeDescend() {
        super("SafeDescend", "Slowly descend using shift key, no fall damage", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (mc.player.isOnGround()) return;

        boolean sneakHeld = mc.options.sneakKey.isPressed();
        boolean falling   = mc.player.getVelocity().y < 0;

        boolean shouldDescend = (sneakHeld && falling) || (autoActivate.isEnabled() && falling);
        if (!shouldDescend) return;

        Vec3d vel = mc.player.getVelocity();
        mc.player.setVelocity(vel.x, -descendSpeed.get(), vel.z);
        mc.player.fallDistance = 0;
    }
}
