package cc.quark.module.modules.staff;

import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.ModeSetting;
import cc.quark.setting.StringSetting;
import cc.quark.util.ChatUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

public class PermBan extends Module {

    private final StringSetting target = register(new StringSetting(
            "Target", "Player name to permanently ban (leave blank to use crosshair)", ""));
    private final StringSetting reason = register(new StringSetting(
            "Reason", "Ban reason appended to the command", "Cheating"));
    private final ModeSetting banCommand = register(new ModeSetting(
            "Command", "Command used for the permanent ban", "ban", "ban", "permban", "ban-ip"));
    private final BoolSetting confirm = register(new BoolSetting(
            "Confirm", "Print confirmation message before executing", true));
    private final BoolSetting requireLook = register(new BoolSetting(
            "Require Look", "Use the player you are looking at when target is blank", true));

    public PermBan() {
        super("PermBan", "Permanently bans a player with one command", Category.STAFF, 0);
    }

    @Override
    public void onEnable() {
        if (mc.player == null) { disable(); return; }

        String name = target.get().trim();

        if (name.isEmpty() && requireLook.isEnabled()) {
            HitResult hit = mc.crosshairTarget;
            if (hit instanceof EntityHitResult ehr && ehr.getEntity() instanceof PlayerEntity p) {
                name = p.getName().getString();
            }
        }

        if (name.isEmpty() && mc.world != null) {
            // Fall back to nearest player
            mc.world.getPlayers().stream()
                .filter(p -> p != mc.player)
                .min((a, b) -> Double.compare(
                    a.squaredDistanceTo(mc.player), b.squaredDistanceTo(mc.player)))
                .ifPresent(p -> {});
            // Re-assign from nearest
            var nearest = mc.world.getPlayers().stream()
                .filter(p -> p != mc.player)
                .min((a, b) -> Double.compare(
                    a.squaredDistanceTo(mc.player), b.squaredDistanceTo(mc.player)))
                .orElse(null);
            if (nearest != null) name = nearest.getName().getString();
        }

        if (name.isEmpty()) {
            ChatUtil.warn("§6[PermBan] §cNo target found. Set a Target name or look at a player.");
            disable();
            return;
        }

        String cmd = banCommand.get() + " " + name + " " + reason.get();
        if (confirm.isEnabled()) {
            ChatUtil.info("§6[PermBan] §fExecuting: §7/" + cmd);
        }
        mc.player.networkHandler.sendChatCommand(cmd);
        ChatUtil.info("§6[PermBan] §cBanned §e" + name + " §7— " + reason.get());
        disable();
    }
}
