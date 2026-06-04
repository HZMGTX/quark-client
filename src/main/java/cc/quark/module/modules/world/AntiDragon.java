package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.util.Hand;

/**
 * AntiDragon - Automatically attacks End Crystals within range to destroy them.
 */
public class AntiDragon extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Attack range for end crystals", 8.0, 2.0, 12.0));

    private final TimerUtil timer = new TimerUtil();

    public AntiDragon() {
        super("AntiDragon", "Destroys dragon crystals automatically", Category.WORLD);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(100)) return;

        for (var entity : mc.world.getEntities()) {
            if (!(entity instanceof EndCrystalEntity crystal)) continue;
            double dist = mc.player.distanceTo(crystal);
            if (dist > range.get()) continue;

            mc.interactionManager.attackEntity(mc.player, crystal);
            mc.player.swingHand(Hand.MAIN_HAND);
            timer.reset();
            return;
        }
    }
}
