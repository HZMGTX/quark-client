package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.StringSetting;
import cc.quark.util.ChatUtil;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * PickupFilter - Prevents the player from picking up items that are on a
 * blacklist, or only allows picking up items on a whitelist.
 *
 * Implemented by teleporting unwanted item entities just out of reach every
 * tick so vanilla magnet-range logic skips them.
 */
public class PickupFilter extends Module {

    private final BoolSetting whitelistMode = register(new BoolSetting(
            "Whitelist Mode", "true = only pick up listed items; false = block listed items", false));

    private final StringSetting itemList = register(new StringSetting(
            "Item List",
            "Comma-separated item IDs or partial names (e.g. cobblestone,dirt,gravel)",
            "cobblestone,dirt,gravel,flint"));

    private final BoolSetting notifyDropped = register(new BoolSetting(
            "Notify", "Print a warning when an item is blocked", false));

    public PickupFilter() {
        super("PickupFilter", "Filters which ground items the player picks up", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        Set<String> filters = parseList();
        if (filters.isEmpty()) return;

        double pickupRadius = 1.5; // approximate vanilla attract distance
        List<ItemEntity> nearby = mc.world.getEntitiesByClass(
                ItemEntity.class,
                mc.player.getBoundingBox().expand(pickupRadius + 1),
                e -> !e.cannotPickup()
        );

        for (ItemEntity itemEnt : nearby) {
            ItemStack stack = itemEnt.getStack();
            if (stack.isEmpty()) continue;

            String id   = stack.getItem().toString().toLowerCase();
            String name = stack.getName().getString().toLowerCase();

            boolean matches = filters.stream().anyMatch(f -> id.contains(f) || name.contains(f));
            boolean shouldBlock = whitelistMode.isEnabled() != matches;

            if (shouldBlock) {
                // Mark item as recently thrown so player can't pick it up this tick
                itemEnt.setPickupDelay(40);
                if (notifyDropped.isEnabled()) {
                    ChatUtil.warn("[PickupFilter] Blocked: " + stack.getName().getString());
                }
            }
        }
    }

    private Set<String> parseList() {
        Set<String> result = new HashSet<>();
        String raw = itemList.get().trim();
        if (raw.isEmpty()) return result;
        Arrays.stream(raw.split(","))
              .map(String::trim)
              .filter(s -> !s.isEmpty())
              .forEach(result::add);
        return result;
    }
}
