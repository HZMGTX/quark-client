package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.entity.mob.EndermiteEntity;
import net.minecraft.util.Hand;

public class AntiEndermite extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Range to kill endermites", 8.0, 1.0, 20.0));

    public AntiEndermite() {
        super("AntiEndermite", "Kills endermites spawned from pearl throws", Category.WORLD);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        double r = range.get();
        for (var entity : mc.world.getEntities()) {
            if (!(entity instanceof EndermiteEntity endermite)) continue;
            if (mc.player.distanceTo(endermite) > r) continue;
            mc.interactionManager.attackEntity(mc.player, endermite);
            mc.player.swingHand(Hand.MAIN_HAND);
            break;
        }
    }
}
