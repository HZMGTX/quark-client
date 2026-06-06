package cc.quark.module.modules.staff;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.setting.StringSetting;
import cc.quark.util.ChatUtil;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class TempBan extends Module {

    private final StringSetting targetPlayer = register(new StringSetting(
            "Target Player", "Name of the player to temporarily ban", ""));
    private final IntSetting defaultDuration = register(new IntSetting(
            "Duration (Days)", "Default ban length in days", 7, 1, 30));
    private final BoolSetting warnBeforeExpiry = register(new BoolSetting(
            "Warn Before Expiry", "Notify in chat 1 minute before a ban expires", true));

    // Maps player name -> expiry timestamp (epoch ms)
    private final Map<String, Long> banExpiry = new HashMap<>();
    private int tickCounter = 0;

    public TempBan() {
        super("TempBan", "Issues timed bans and auto-unbans players when their ban expires", Category.STAFF);
    }

    @Override
    public void onEnable() {
        if (mc.player == null) { disable(); return; }
        String target = targetPlayer.get().trim();
        if (target.isEmpty()) {
            ChatUtil.warn("[TempBan] Set Target Player before enabling.");
            disable();
            return;
        }
        long expiry = System.currentTimeMillis() + (long) defaultDuration.get() * 86_400_000L;
        banExpiry.put(target, expiry);
        mc.player.networkHandler.sendChatCommand("ban " + target + " Temporary ban (" + defaultDuration.get() + "d)");
        ChatUtil.info("§6[TempBan] §fBanned §e" + target + " §ffor §e" + defaultDuration.get() + " §fday(s).");
        tickCounter = 0;
    }

    @Override
    public void onDisable() {
        banExpiry.clear();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (++tickCounter < 20) return; // check once per second
        tickCounter = 0;

        long now = System.currentTimeMillis();
        Iterator<Map.Entry<String, Long>> iter = banExpiry.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, Long> entry = iter.next();
            String name = entry.getKey();
            long expiry = entry.getValue();
            long remaining = expiry - now;

            if (remaining <= 0) {
                mc.player.networkHandler.sendChatCommand("pardon " + name);
                ChatUtil.success("§6[TempBan] §fAuto-unbanned §e" + name + " §f(ban expired).");
                iter.remove();
            } else if (warnBeforeExpiry.isEnabled() && remaining <= 60_000L && remaining > 59_000L) {
                ChatUtil.warn("§6[TempBan] §eBan for §f" + name + " §eexpires in ~1 minute.");
            }
        }

        if (banExpiry.isEmpty()) disable();
    }
}
