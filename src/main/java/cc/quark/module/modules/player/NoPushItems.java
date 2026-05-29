package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.entity.ItemEntity;

public class NoPushItems extends Module {

    private final BoolSetting noAttract = register(new BoolSetting(
            "No Attract", "Move nearby item entities away each tick", true));

    public NoPushItems() {
        super("NoPushItems", "Prevents nearby item entities from pushing the player around", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        if (noAttract.isEnabled()) {
            // Set velocity of close item entities away from player to prevent collision push
            for (var entity : mc.world.getEntities()) {
                if (!(entity instanceof ItemEntity)) continue;
                if (mc.player.distanceTo(entity) > 1.5) continue;

                // Give item entity a small velocity away from us
                double dx = entity.getX() - mc.player.getX();
                double dz = entity.getZ() - mc.player.getZ();
                double len = Math.sqrt(dx * dx + dz * dz);
                if (len < 0.01) continue;

                entity.setVelocity(dx / len * 0.1, entity.getVelocity().y, dz / len * 0.1);
            }
        }
    }
}
