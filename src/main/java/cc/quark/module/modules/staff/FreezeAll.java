package cc.quark.module.modules.staff;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.setting.ModeSetting;
import cc.quark.setting.StringSetting;
import cc.quark.util.ChatUtil;
import net.minecraft.client.network.PlayerListEntry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class FreezeAll extends Module {

    private final ModeSetting command = register(new ModeSetting(
            "Command", "Freeze command to execute per player", "freeze", "freeze", "halt", "stop", "immobilize"));
    private final StringSetting reason = register(new StringSetting(
            "Reason", "Reason shown when players are frozen", "Staff inspection"));
    private final BoolSetting skipSelf = register(new BoolSetting(
            "Skip Self", "Do not freeze yourself", true));
    private final IntSetting delayTicks = register(new IntSetting(
            "Delay Ticks", "Ticks between each freeze command", 3, 1, 20));
    private final BoolSetting announce = register(new BoolSetting(
            "Announce", "Broadcast a freeze notice to all players", true));

    private final List<String> queue = new ArrayList<>();
    private int tickCounter = 0;
    private boolean initialized = false;

    public FreezeAll() {
        super("FreezeAll", "Freezes all online players simultaneously", Category.STAFF, 0);
    }

    @Override
    public void onEnable() {
        if (mc.player == null || mc.getNetworkHandler() == null) { disable(); return; }
        queue.clear();
        tickCounter = 0;
        initialized = false;
        ChatUtil.info("§6[FreezeAll] §fQueuing freeze commands...");
    }

    @Override
    public void onDisable() {
        queue.clear();
        initialized = false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.getNetworkHandler() == null) { disable(); return; }

        if (!initialized) {
            Collection<PlayerListEntry> entries = mc.getNetworkHandler().getPlayerList();
            String selfName = mc.player.getGameProfile().getName();
            for (PlayerListEntry entry : entries) {
                String name = entry.getProfile().getName();
                if (name == null || name.isEmpty()) continue;
                if (skipSelf.isEnabled() && name.equalsIgnoreCase(selfName)) continue;
                queue.add(name);
            }
            initialized = true;
            if (announce.isEnabled()) {
                mc.player.networkHandler.sendChatCommand(
                        "say [STAFF] All players have been frozen: " + reason.get());
            }
            ChatUtil.info("§6[FreezeAll] §f" + queue.size() + " player(s) queued.");
            if (queue.isEmpty()) { disable(); return; }
        }

        if (++tickCounter < delayTicks.get()) return;
        tickCounter = 0;

        if (queue.isEmpty()) {
            ChatUtil.info("§6[FreezeAll] §fAll players frozen.");
            disable();
            return;
        }

        String target = queue.remove(0);
        mc.player.networkHandler.sendChatCommand(command.get() + " " + target);
        ChatUtil.info("§6[FreezeAll] §fFroze: §e" + target);
    }
}
