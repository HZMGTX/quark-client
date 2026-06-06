package cc.quark.module.modules.staff;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.StringSetting;
import cc.quark.util.ChatUtil;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;

import java.util.UUID;

/**
 * PlayerStats - Displays detailed real-time stats for a target player.
 * Updates every second and prints to local chat.
 */
public class PlayerStats extends Module {

    private final StringSetting targetName = register(new StringSetting(
            "Target", "Player name to inspect", ""));

    private final BoolSetting showHealth = register(new BoolSetting(
            "Health", "Show health and armor", true));

    private final BoolSetting showPosition = register(new BoolSetting(
            "Position", "Show XYZ coordinates", true));

    private final BoolSetting showPing = register(new BoolSetting(
            "Ping", "Show network ping from tab list", true));

    private final BoolSetting showGameMode = register(new BoolSetting(
            "Gamemode", "Show player's gamemode", true));

    private final BoolSetting showMotion = register(new BoolSetting(
            "Motion", "Show movement speed estimate", false));

    private long lastUpdateMs = 0;
    private static final long UPDATE_INTERVAL = 1000;

    public PlayerStats() {
        super("PlayerStats", "Shows detailed real-time stats for a target player", Category.STAFF, 0);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        long now = System.currentTimeMillis();
        if (now - lastUpdateMs < UPDATE_INTERVAL) return;
        lastUpdateMs = now;

        String name = targetName.get().trim();
        if (name.isEmpty()) {
            ChatUtil.warn("[PlayerStats] Set a target name first.");
            return;
        }

        // Find the target in the loaded world
        PlayerEntity target = null;
        for (PlayerEntity p : mc.world.getPlayers()) {
            if (p.getName().getString().equalsIgnoreCase(name)) {
                target = p;
                break;
            }
        }

        if (target == null) {
            ChatUtil.warn("[PlayerStats] Player '" + name + "' not found in range.");
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("§b[PlayerStats] §f").append(target.getName().getString()).append(" §8|");

        if (showHealth.isEnabled()) {
            float hp  = target.getHealth();
            float maxHp = target.getMaxHealth();
            float armor = target.getArmor();
            sb.append(" §cHP: §f").append(String.format("%.1f/%.1f", hp, maxHp));
            sb.append(" §7Armor: §f").append((int) armor);
        }

        if (showPosition.isEnabled()) {
            sb.append(" §aXYZ: §f")
              .append(String.format("%.1f, %.1f, %.1f",
                      target.getX(), target.getY(), target.getZ()));
        }

        if (showMotion.isEnabled()) {
            double speed = MathHelper.sqrt((float)(
                    target.getVelocity().x * target.getVelocity().x
                    + target.getVelocity().z * target.getVelocity().z));
            sb.append(" §eSpeed: §f").append(String.format("%.2f b/t", speed));
        }

        if (showGameMode.isEnabled()) {
            String gm = target.getAbilities().creativeMode ? "Creative"
                      : target.getAbilities().invulnerable ? "Spectator/God"
                      : "Survival";
            sb.append(" §dGM: §f").append(gm);
        }

        if (showPing.isEnabled() && mc.getNetworkHandler() != null) {
            UUID uuid = target.getUuid();
            PlayerListEntry entry = mc.getNetworkHandler().getPlayerListEntry(uuid);
            if (entry != null) {
                sb.append(" §3Ping: §f").append(entry.getLatency()).append("ms");
            }
        }

        ChatUtil.addMessage(sb.toString());
    }
}
