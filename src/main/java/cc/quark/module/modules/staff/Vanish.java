package cc.quark.module.modules.staff;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;

public class Vanish extends Module {

    private final BoolSetting invisible = register(new BoolSetting(
            "Invisible", "Make client entity invisible to reduce visual detectability", true));

    private final BoolSetting noParticles = register(new BoolSetting(
            "No Particles", "Suppress step and movement particles", true));

    public Vanish() {
        super("Vanish", "Hide completely from regular players.", Category.STAFF, 0);
    }

    @Override
    public void onEnable() {
        if (mc.player == null) return;
        apply();
    }

    @Override
    public void onDisable() {
        if (mc.player == null) return;
        // Restore visible state and stop sneaking
        mc.player.setInvisible(false);
        mc.player.setSneaking(false);
        mc.player.lastSneaking = false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        apply();
    }

    private void apply() {
        if (invisible.isEnabled()) {
            // Set client-side invisible flag so other entities treat the player as invisible
            mc.player.setInvisible(true);
            // Sneaking lowers the detection radius in vanilla and signals to some plugins
            mc.player.setSneaking(true);
            mc.player.lastSneaking = true;
        } else {
            mc.player.setInvisible(false);
            mc.player.setSneaking(false);
            mc.player.lastSneaking = false;
        }

        if (noParticles.isEnabled()) {
            // Prevent the player's step-sound/particle logic by zeroing horizontal
            // velocity seen by particle spawners. We do this by marking the player
            // as not sprinting (cosmetic only on client side).
            mc.player.setSprinting(false);
        }
    }
}
