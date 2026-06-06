package cc.quark.module.modules.staff;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.ChatUtil;
import net.minecraft.entity.player.PlayerEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Polls SuspicionTracker scores each tick; when a player crosses banThreshold
 * it either executes the ban command immediately or queues a confirmation prompt.
 */
public class AutoBanSystem extends Module {

    private final IntSetting banThreshold = register(new IntSetting(
            "Ban Threshold", "Suspicion score required to trigger an automatic ban", 100, 50, 200));
    private final IntSetting banDuration = register(new IntSetting(
            "Ban Duration (days)", "Duration of the temporary ban in days", 7, 1, 30));
    private final BoolSetting requireConfirmation = register(new BoolSetting(
            "Require Confirmation", "Print a warning instead of banning immediately", true));

    // UUID -> whether we already issued a warning / ban action this session
    private final Map<UUID, Boolean> processed = new HashMap<>();

    public AutoBanSystem() {
        super("AutoBanSystem", "Auto-bans players whose suspicion score exceeds the configured threshold", Category.STAFF, 0);
    }

    @Override
    public void onEnable() {
        if (mc.player == null) { disable(); return; }
        processed.clear();
        mc.getEventBus().subscribe(this);
        ChatUtil.info("§6[AutoBanSystem] §fAuto-ban armed — threshold §e" + banThreshold.get()
                + "§f pts, duration §e" + banDuration.get() + "d§f, confirm=§e" + requireConfirmation.isEnabled() + "§f.");
    }

    @Override
    public void onDisable() {
        mc.getEventBus().unsubscribe(this);
        processed.clear();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.world == null || mc.player == null) return;

        Map<UUID, SuspicionTracker.PlayerSuspicion> scores = SuspicionTracker.getScores();
        if (scores == null) return;

        for (Map.Entry<UUID, SuspicionTracker.PlayerSuspicion> entry : scores.entrySet()) {
            UUID id = entry.getKey();
            SuspicionTracker.PlayerSuspicion data = entry.getValue();

            if (data.score < banThreshold.get()) continue;
            if (Boolean.TRUE.equals(processed.get(id))) continue;

            processed.put(id, true);
            String name = data.name;

            // Verify player is still online before acting
            boolean online = mc.world.getPlayers().stream()
                    .anyMatch(p -> p.getUuid().equals(id));

            if (requireConfirmation.isEnabled()) {
                ChatUtil.warn("§c[AutoBanSystem] §e" + name + " §freached §c" + data.score
                        + "§f suspicion pts. Run §e/ban " + name + " §fto confirm.");
            } else {
                if (online) {
                    String cmd = "tempban " + name + " " + banDuration.get() + "d Auto-ban: suspicion score " + data.score;
                    mc.player.networkHandler.sendChatCommand(cmd);
                    ChatUtil.success("§a[AutoBanSystem] §fBanned §e" + name
                            + " §ffor §e" + banDuration.get() + "d§f (score §c" + data.score + "§f).");
                    CheatBroadcast.broadcast("§c[AUTO-BAN] §e" + name + " §fbanned — score §c" + data.score, "High");
                } else {
                    ChatUtil.warn("§6[AutoBanSystem] §e" + name + " §fis no longer online — ban skipped.");
                }
            }
        }
    }
}
