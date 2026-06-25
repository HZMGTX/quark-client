package cc.quark.module.modules.staff;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.ModeSetting;
import cc.quark.setting.StringSetting;
import cc.quark.util.ChatUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

public class BanHammer extends Module {

    private final ModeSetting command = register(new ModeSetting(
            "Command", "Ban command template (use {player} for name)",
            "Ban", "Ban", "TempBan", "Kick", "IPBan", "Mute"));
    private final BoolSetting confirm = register(new BoolSetting(
            "Confirm", "Show confirmation in chat before executing", true));
    private final BoolSetting requireLook = register(new BoolSetting(
            "Require Look", "Execute only on the player you're looking at", true));
    private final StringSetting reason = register(new StringSetting(
            "Reason", "Reason appended to the ban command", "Cheating"));

    public BanHammer() {
        super("BanHammer", "Quickly ban players via command on enable", Category.STAFF, 0);
    }

    @Override
    public void onEnable() {
        if (mc.player == null) { disable(); return; }

        String target = null;

        if (requireLook.isEnabled()) {
            HitResult hit = mc.crosshairTarget;
            if (hit instanceof EntityHitResult ehr && ehr.getEntity() instanceof PlayerEntity p) {
                target = p.getName().getString();
            }
        } else {
            // Use the nearest player
            var nearest = mc.world.getPlayers().stream()
                .filter(p -> p != mc.player)
                .min((a, b) -> Double.compare(
                    a.squaredDistanceTo(mc.player), b.squaredDistanceTo(mc.player)))
                .orElse(null);
            if (nearest != null) target = nearest.getName().getString();
        }

        if (target == null) {
            ChatUtil.warn("[BanHammer] No player targeted.");
            disable(); return;
        }

        String cmd = buildCommand(target);
        if (confirm.isEnabled()) {
            ChatUtil.info("[BanHammer] Executing: /" + cmd);
        }
        mc.player.networkHandler.sendChatCommand(cmd);
        disable();
    }

    private String buildCommand(String player) {
        String reason = this.reason.get();
        return switch (command.get()) {
            case "TempBan" -> "tempban " + player + " 7d " + reason;
            case "Kick"    -> "kick " + player + " " + reason;
            case "IPBan"   -> "ban-ip " + player + " " + reason;
            case "Mute"    -> "mute " + player + " " + reason;
            default        -> "ban " + player + " " + reason;
        };
    }
}
