package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.entity.projectile.FireworkRocketEntity;

public class AntiFirework extends Module {

    private final BoolSetting sneakOnDetect = register(new BoolSetting(
            "SneakOnDetect", "Crouch when an incoming firework is detected nearby", true));

    public AntiFirework() {
        super("AntiFirework", "Tracks incoming fireworks and sneaks to reduce explosion damage", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        boolean fireworkNearby = mc.world.getEntitiesByClass(
                FireworkRocketEntity.class,
                mc.player.getBoundingBox().expand(10),
                fw -> {
                    if (!(fw instanceof FireworkRocketEntity rocket)) return false;
                    return rocket.wasShotByEntity();
                }
        ).stream().anyMatch(fw -> {
            var owner = ((FireworkRocketEntity) fw).getOwner();
            return owner == null || owner != mc.player;
        });

        if (sneakOnDetect.isEnabled()) {
            mc.options.sneakKey.setPressed(fireworkNearby);
        }
    }
}
