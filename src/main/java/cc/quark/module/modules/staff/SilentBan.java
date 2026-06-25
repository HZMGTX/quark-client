package cc.quark.module.modules.staff;

import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.StringSetting;
import cc.quark.util.ChatUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Bans a targeted player silently: kicks them with a fake "left the game"
 * message so other players see a normal disconnect rather than a ban notice.
 *
 * Usage: aim at the target player and toggle the module on.
 * The module disables itself after executing.
 */
public class SilentBan extends Module {

    private final StringSetting fakeMsg = register(new StringSetting(
            "Fake Message", "Disconnect message shown to the server", "Player left the game"));
    private final BoolSetting saveToLog = register(new BoolSetting(
            "Save To Log", "Append ban record to quark/silentban.log", true));

    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public SilentBan() {
        super("SilentBan", "Bans the targeted player with a fake disconnect message to avoid public notice", Category.STAFF, 0);
    }

    @Override
    public void onEnable() {
        if (mc.player == null || mc.world == null) { disable(); return; }

        PlayerEntity target = resolveTarget();
        if (target == null) {
            ChatUtil.warn("§c[SilentBan] §fNo player targeted. Aim at a player first.");
            disable();
            return;
        }

        String name = target.getName().getString();
        String uuid = target.getUuidAsString();

        // Step 1: Kick silently with the fake leave message via /kick
        // (requires OP / server-side permission — this client sends the command)
        String kickCmd = "kick " + name + " " + fakeMsg.get();
        mc.player.networkHandler.sendChatCommand(kickCmd);

        // Step 2: Immediately ban the player so they cannot reconnect
        String banCmd = "ban " + name + " Silent ban";
        mc.player.networkHandler.sendChatCommand(banCmd);

        ChatUtil.success("§a[SilentBan] §fSilently banned §e" + name
                + " §7(UUID: " + uuid + ")§f with message: §7\"" + fakeMsg.get() + "\"");

        // Step 3: Optionally log to file
        if (saveToLog.isEnabled()) {
            logBan(name, uuid);
        }

        // Notify the CheatBroadcast system (staff-only channel) without public broadcast
        CheatBroadcast.broadcast("§c[SilentBan] §e" + name + " §fwas silently banned.", "High");

        disable();
    }

    @Override
    public void onDisable() {
        // Nothing to clean up — module is one-shot
    }

    private PlayerEntity resolveTarget() {
        // Prefer crosshair look target
        HitResult hit = mc.crosshairTarget;
        if (hit instanceof EntityHitResult ehr && ehr.getEntity() instanceof PlayerEntity p) {
            return p;
        }
        // Fall back to nearest player within 10 blocks
        return mc.world.getPlayers().stream()
                .filter(p -> p != mc.player && p.distanceTo(mc.player) <= 10.0)
                .min((a, b) -> Double.compare(a.distanceTo(mc.player), b.distanceTo(mc.player)))
                .orElse(null);
    }

    private void logBan(String name, String uuid) {
        try {
            File dir = new File(mc.runDirectory, "quark");
            if (!dir.exists()) dir.mkdirs();
            try (PrintWriter pw = new PrintWriter(new FileWriter(new File(dir, "silentban.log"), true))) {
                pw.println("[" + DATE_FMT.format(new Date()) + "] SILENT-BAN | Player: " + name
                        + " | UUID: " + uuid + " | FakeMsg: " + fakeMsg.get());
            }
        } catch (IOException e) {
            ChatUtil.warn("§c[SilentBan] §fFailed to write log: " + e.getMessage());
        }
    }
}
