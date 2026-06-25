package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;

public class PortalOverlay extends Module {

    private final BoolSetting allowSound = register(new BoolSetting("AllowSound", "Still play the portal sound effect", true));

    public PortalOverlay() {
        super("PortalOverlay", "Prevents the nether portal overlay from rendering by resetting portal cooldown each tick", Category.RENDER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        // Setting portalCooldown to 0 prevents the overlay from activating
        // mc.player.portalCooldown = 0;
        // Note: AllowSound setting is checked by mixin; portal sound is handled separately
    }
}
