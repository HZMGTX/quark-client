package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.util.Hand;

public class AntiGhast2 extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Range to detect ghast fireballs", 8.0, 2.0, 20.0));

    private final BoolSetting deflect = register(new BoolSetting(
            "Deflect", "Reflect fireballs back at ghast", true));

    public AntiGhast2() {
        super("AntiGhast2", "Deflects ghast fireballs back", Category.WORLD);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        double r = range.get();
        for (var entity : mc.world.getEntities()) {
            if (!(entity instanceof FireballEntity fireball)) continue;
            if (mc.player.distanceTo(fireball) > r) continue;

            if (deflect.isEnabled()) {
                // Hit the fireball back
                mc.interactionManager.attackEntity(mc.player, fireball);
                mc.player.swingHand(Hand.MAIN_HAND);
                break;
            }
        }
    }
}
