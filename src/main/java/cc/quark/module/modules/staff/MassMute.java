package cc.quark.module.modules.staff;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.ChatUtil;
import net.minecraft.client.network.PlayerListEntry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MassMute extends Module {

    private final IntSetting range = register(new IntSetting(
            "Range", "Block radius to scan (-1 = entire server)", 64, 16, 256));
    private final BoolSetting staffExempt = register(new BoolSetting(
            "Staff Exempt", "Skip players in operator/staff role (by name list placeholder)", true));
    private final IntSetting duration = register(new IntSetting(
            "Duration (Minutes)", "How many minutes players stay muted", 10, 1, 60));
    private final IntSetting delayTicks = register(new IntSetting(
            "Delay Ticks", "Ticks between each mute command to avoid spam", 3, 1, 20));

    private final List<String> muteQueue = new ArrayList<>();
    private int tickCounter = 0;
    private boolean initialized = false;

    public MassMute() {
        super("MassMute", "Mutes all non-staff players within a radius or the whole server", Category.STAFF);
    }

    @Override
    public void onEnable() {
        if (mc.player == null || mc.getNetworkHandler() == null) { disable(); return; }
        muteQueue.clear();
        tickCounter = 0;
        initialized = false;
        ChatUtil.info("§6[MassMute] §fCollecting players to mute...");
    }

    @Override
    public void onDisable() {
        muteQueue.clear();
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
                if (name == null || name.isEmpty() || name.equalsIgnoreCase(selfName)) continue;
                if (staffExempt.isEnabled() && isStaffName(name)) continue;
                if (range.get() < 256) {
                    // Filter by range using visible world players
                    if (mc.world != null) {
                        boolean inRange = mc.world.getPlayers().stream()
                                .anyMatch(p -> p.getName().getString().equalsIgnoreCase(name)
                                        && p.distanceTo(mc.player) <= range.get());
                        if (!inRange) continue;
                    }
                }
                muteQueue.add(name);
            }
            initialized = true;
            ChatUtil.info("§6[MassMute] §fQueued §e" + muteQueue.size() + " §fplayer(s) for mute.");
            if (muteQueue.isEmpty()) { disable(); return; }
        }

        if (++tickCounter < delayTicks.get()) return;
        tickCounter = 0;

        if (muteQueue.isEmpty()) {
            ChatUtil.success("§6[MassMute] §fAll queued players muted.");
            disable();
            return;
        }

        String target = muteQueue.remove(0);
        mc.player.networkHandler.sendChatCommand("mute " + target + " " + duration.get() + "m Staff action");
        ChatUtil.info("§6[MassMute] §fMuted: §e" + target);
    }

    /** Simple heuristic — extend this list to match your server's staff convention. */
    private boolean isStaffName(String name) {
        String lower = name.toLowerCase();
        return lower.contains("admin") || lower.contains("mod") || lower.contains("staff");
    }
}
