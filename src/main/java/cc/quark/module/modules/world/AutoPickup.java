package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.StringSetting;
import net.minecraft.entity.ItemEntity;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Vec3d;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * AutoPickup - Moves the player towards nearby item entities to auto-collect them.
 * Filter by item name (comma-separated), or pick up everything.
 */
public class AutoPickup extends Module {

    private final StringSetting filter = register(new StringSetting(
            "Filter", "Comma-separated item names to pick up (empty = all)", ""));
    private final BoolSetting allItems = register(new BoolSetting(
            "AllItems", "Pick up all items regardless of filter", true));

    public AutoPickup() {
        super("AutoPickup", "Auto-picks up specific items by name", Category.WORLD);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        Set<String> names = getFilterSet();

        ItemEntity nearest = null;
        double closestDist = 5.0; // Only attract items within 5 blocks

        for (var entity : mc.world.getEntities()) {
            if (!(entity instanceof ItemEntity itemEnt)) continue;
            String itemName = itemEnt.getStack().getName().getString().toLowerCase();

            if (!allItems.isEnabled() && !names.isEmpty()) {
                boolean matches = names.stream().anyMatch(itemName::contains);
                if (!matches) continue;
            }

            double dist = mc.player.distanceTo(itemEnt);
            if (dist < closestDist) {
                closestDist = dist;
                nearest = itemEnt;
            }
        }

        if (nearest == null) return;

        // Move toward item
        Vec3d itemPos = nearest.getPos();
        Vec3d playerPos = mc.player.getPos();
        Vec3d dir = itemPos.subtract(playerPos).normalize().multiply(0.2);

        mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
                playerPos.x + dir.x,
                playerPos.y,
                playerPos.z + dir.z,
                mc.player.isOnGround()));
    }

    private Set<String> getFilterSet() {
        Set<String> set = new HashSet<>();
        String raw = filter.get().trim();
        if (!raw.isEmpty()) {
            for (String s : raw.split(",")) {
                String trimmed = s.trim().toLowerCase();
                if (!trimmed.isEmpty()) set.add(trimmed);
            }
        }
        return set;
    }
}
