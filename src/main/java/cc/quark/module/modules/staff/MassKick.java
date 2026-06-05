package cc.quark.module.modules.staff;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.setting.StringSetting;
import cc.quark.util.ChatUtil;
import net.minecraft.client.network.PlayerListEntry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MassKick extends Module {

    private final StringSetting reason = register(new StringSetting(
            "Reason", "Kick reason sent to each player", "Kicked by staff"));
    private final BoolSetting skipSelf = register(new BoolSetting(
            "Skip Self", "Do not kick yourself", true));
    private final BoolSetting skipOps = register(new BoolSetting(
            "Skip Ops", "Skip players in the ops whitelist (name contains 'op')", false));
    private final IntSetting delayTicks = register(new IntSetting(
            "Delay Ticks", "Ticks to wait between each kick command", 5, 1, 40));

    private final List<String> kickQueue = new ArrayList<>();
    private int tickCounter = 0;
    private boolean initialized = false;

    public MassKick() {
        super("MassKick", "Sends kick commands for all online players", Category.STAFF, 0);
    }

    @Override
    public void onEnable() {
        if (mc.player == null || mc.getNetworkHandler() == null) { disable(); return; }
        mc.getEventBus().subscribe(this);
        kickQueue.clear();
        tickCounter = 0;
        initialized = false;
        ChatUtil.info("§6[MassKick] §fQueuing kick commands...");
    }

    @Override
    public void onDisable() {
        mc.getEventBus().unsubscribe(this);
        kickQueue.clear();
        initialized = false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.getNetworkHandler() == null) { disable(); return; }

        // Build the kick queue on first tick
        if (!initialized) {
            Collection<PlayerListEntry> entries = mc.getNetworkHandler().getPlayerList();
            String selfName = mc.player.getGameProfile().getName();
            for (PlayerListEntry entry : entries) {
                String name = entry.getProfile().getName();
                if (name == null || name.isEmpty()) continue;
                if (skipSelf.isEnabled() && name.equalsIgnoreCase(selfName)) continue;
                kickQueue.add(name);
            }
            initialized = true;
            ChatUtil.info("§6[MassKick] §f" + kickQueue.size() + " player(s) queued.");
            if (kickQueue.isEmpty()) { disable(); return; }
        }

        // Send one kick per delay interval
        if (++tickCounter < delayTicks.get()) return;
        tickCounter = 0;

        if (kickQueue.isEmpty()) {
            ChatUtil.info("§6[MassKick] §fAll kick commands sent.");
            disable();
            return;
        }

        String target = kickQueue.remove(0);
        String cmd = "kick " + target + " " + reason.get();
        mc.player.networkHandler.sendChatCommand(cmd);
        ChatUtil.info("§6[MassKick] §fKicking: §e" + target);
    }
}
