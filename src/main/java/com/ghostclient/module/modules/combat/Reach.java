package com.ghostclient.module.modules.combat;

import com.ghostclient.event.EventHandler;
import com.ghostclient.event.events.EventTick;
import com.ghostclient.module.Category;
import com.ghostclient.module.Module;
import com.ghostclient.setting.BoolSetting;
import com.ghostclient.setting.DoubleSetting;

import java.util.Random;

/**
 * Reach - extends the player's attack reach beyond the vanilla limit.
 *
 * <p>Because Minecraft Fabric's {@code ClientPlayerInteractionManager} checks
 * {@code attackRange} via a field that reflects the player's attribute, we
 * modify the reach by setting the attack distance field directly every tick
 * through reflection (or via a mixin in the actual codebase).
 *
 * <p>This class stores the configured range as a public static field that a
 * companion mixin ({@code MixinClientPlayerInteractionManager}) can read to
 * override the attack-range check.
 */
public class Reach extends Module {

    /** Public reach value read by the companion mixin. */
    public static double currentReach = 3.0;
    /** Whether to jitter the reach slightly for a more "legit" appearance. */
    public static boolean legitMode = false;

    private static final Random RNG = new Random();

    private final DoubleSetting reach = register(new DoubleSetting(
            "Reach", "Maximum attack range in blocks", 3.5, 3.0, 6.0));

    private final BoolSetting legit = register(new BoolSetting(
            "Legit", "Randomise reach slightly to appear more human", false));

    public Reach() {
        super("Reach", "Extends the player attack range beyond 3 blocks", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        updateStatics();
    }

    @Override
    public void onDisable() {
        // Reset to vanilla default
        currentReach = 3.0;
        legitMode    = false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        updateStatics();
    }

    private void updateStatics() {
        legitMode = legit.isEnabled();
        if (legitMode) {
            // Randomise in a ±0.15 window around configured value so reach varies each tick
            currentReach = reach.get() + (RNG.nextDouble() * 0.3 - 0.15);
        } else {
            currentReach = reach.get();
        }
    }
}
