package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;

/**
 * AFKMode - slowly rotates the player's yaw so the session looks active.
 */
public class AFKMode extends Module {

    private final IntSetting speed = register(new IntSetting("Speed", "Degrees per tick", 2, 1, 20));

    public AFKMode() {
        super("AFKMode", "Slowly spins the player to stay active", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        mc.player.setYaw(mc.player.getYaw() + speed.get());
    }
}
