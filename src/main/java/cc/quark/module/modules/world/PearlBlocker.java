package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.util.ChatUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;

import java.util.HashSet;
import java.util.Set;

/**
 * PearlBlocker - detects enemy ender pearl projectiles near the player.
 * Alerts and tracks incoming pearls to help players react defensively.
 * (Cannot actually intercept physics-based entities on client side without server cooperation.)
 */
public class PearlBlocker extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Detection range for enemy enderpearls", 8.0, 2.0, 16.0));

    private final Set<Integer> alertedIds = new HashSet<>();

    public PearlBlocker() {
        super("PearlBlocker", "Detects and alerts about nearby enemy enderpearl entities", Category.WORLD);
    }

    @Override
    public void onEnable() {
        alertedIds.clear();
    }

    @Override
    public void onDisable() {
        alertedIds.clear();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        double rangeSq = range.get() * range.get();

        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof EnderPearlEntity pearl)) continue;
            // Ignore our own pearls
            if (pearl.getOwner() != null && pearl.getOwner().equals(mc.player)) continue;

            double distSq = mc.player.squaredDistanceTo(pearl);
            if (distSq > rangeSq) {
                alertedIds.remove(pearl.getId());
                continue;
            }

            if (!alertedIds.contains(pearl.getId())) {
                alertedIds.add(pearl.getId());
                String ownerName = pearl.getOwner() != null
                        ? pearl.getOwner().getName().getString()
                        : "Unknown";
                int dist = (int) Math.sqrt(distSq);
                ChatUtil.warn("[PearlBlocker] Incoming pearl from " + ownerName + " (" + dist + "m away)");
            }
        }

        // Clean up IDs of entities that no longer exist
        alertedIds.removeIf(id -> mc.world.getEntityById(id) == null);
    }
}
