package cc.quark.module.modules.staff;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.StringSetting;
import cc.quark.util.ChatUtil;
import net.minecraft.client.network.PlayerListEntry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ItemRemoverStaff extends Module {

    private final StringSetting targetPlayer = register(new StringSetting(
            "Target Player", "Name of the player to remove items from (ignored if Check All)", ""));
    private final StringSetting itemName = register(new StringSetting(
            "Item Name", "Minecraft item ID to remove (e.g. minecraft:diamond_sword)", ""));
    private final BoolSetting checkAll = register(new BoolSetting(
            "Check All", "Run the remove command on all online players", false));

    private final List<String> removeQueue = new ArrayList<>();
    private int tickCounter = 0;
    private boolean initialized = false;

    public ItemRemoverStaff() {
        super("ItemRemoverStaff", "Removes a specific item type from one or all players' inventories", Category.STAFF);
    }

    @Override
    public void onEnable() {
        if (mc.player == null) { disable(); return; }
        String item = itemName.get().trim();
        if (item.isEmpty()) {
            ChatUtil.warn("[ItemRemoverStaff] Set Item Name before enabling.");
            disable();
            return;
        }
        removeQueue.clear();
        tickCounter = 0;
        initialized = false;
    }

    @Override
    public void onDisable() {
        removeQueue.clear();
        initialized = false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.getNetworkHandler() == null) { disable(); return; }

        if (!initialized) {
            if (checkAll.isEnabled()) {
                Collection<PlayerListEntry> entries = mc.getNetworkHandler().getPlayerList();
                for (PlayerListEntry entry : entries) {
                    String name = entry.getProfile().getName();
                    if (name != null && !name.isEmpty()) removeQueue.add(name);
                }
            } else {
                String target = targetPlayer.get().trim();
                if (target.isEmpty()) {
                    ChatUtil.warn("[ItemRemoverStaff] Set Target Player or enable Check All.");
                    disable();
                    return;
                }
                removeQueue.add(target);
            }
            initialized = true;
            ChatUtil.info("§6[ItemRemoverStaff] §fRemoving §e" + itemName.get()
                    + " §ffrom §e" + removeQueue.size() + " §fplayer(s).");
        }

        if (++tickCounter < 3) return;
        tickCounter = 0;

        if (removeQueue.isEmpty()) {
            ChatUtil.success("§6[ItemRemoverStaff] §fItem removal complete.");
            disable();
            return;
        }

        String target = removeQueue.remove(0);
        mc.player.networkHandler.sendChatCommand("clear " + target + " " + itemName.get());
        ChatUtil.info("§6[ItemRemoverStaff] §fRemoved §e" + itemName.get() + " §ffrom §e" + target);
    }
}
