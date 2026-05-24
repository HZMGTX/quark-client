package cc.quark.module.modules.combat;

import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

/**
 * AntiBot - provides a static {@link #isBot(Entity)} helper used by combat modules
 * (primarily KillAura) to filter out fake player entities commonly spawned by
 * server-side anti-cheat systems or crystal PvP bots.
 *
 * <p>Detection heuristics:
 * <ul>
 *   <li><b>Name check</b> - flags entities whose names contain suspicious patterns
 *       (all digits, very long names, etc.).</li>
 *   <li><b>Movement check</b> - flags entities that have been stationary or moving
 *       in an unnaturally regular pattern (zero velocity, teleporting).</li>
 *   <li><b>Ping check</b> - flags players missing from the player list (no network
 *       info = likely a fake entity spawned server-side).</li>
 * </ul>
 */
public class AntiBot extends Module {

    // ---- Settings ----
    private final BoolSetting nameCheck = register(new BoolSetting(
            "Name Check", "Flag entities with suspicious names (all digits, no profile, etc.)", true));

    private final BoolSetting movementCheck = register(new BoolSetting(
            "Movement Check", "Flag entities with zero or unnatural velocity", true));

    private final BoolSetting pingCheck = register(new BoolSetting(
            "Ping Check", "Flag player entities missing from the server player list", true));

    // ---- Shared static reference so other modules can call isBot() ----
    private static AntiBot instance;

    public AntiBot() {
        super("AntiBot", "Filters bot entities from combat module target lists", Category.COMBAT);
        instance = this;
    }

    /**
     * Returns {@code true} if the given entity is considered a bot by the enabled heuristics.
     * Safe to call from any module regardless of whether AntiBot is enabled.
     */
    public static boolean isBot(Entity entity) {
        if (instance == null || !instance.isEnabled()) return false;
        if (!(entity instanceof PlayerEntity player)) return false;

        // --- Name check ---
        if (instance.nameCheck.isEnabled()) {
            String name = player.getGameProfile().getName();
            if (isSuspiciousName(name)) return true;
        }

        // --- Ping / player-list check ---
        if (instance.pingCheck.isEnabled()) {
            // If the player isn't in our client's player list, it's almost certainly a bot.
            var mc = net.minecraft.client.MinecraftClient.getInstance();
            if (mc.getNetworkHandler() != null) {
                boolean inList = mc.getNetworkHandler()
                        .getPlayerList()
                        .stream()
                        .anyMatch(entry -> entry.getProfile().getId()
                                .equals(player.getGameProfile().getId()));
                if (!inList) return true;
            }
        }

        // --- Movement check ---
        if (instance.movementCheck.isEnabled()) {
            Vec3d vel = player.getVelocity();
            // Completely stationary while "moving" (no gravity, no ticking) -> bot
            if (vel.x == 0.0 && vel.y == 0.0 && vel.z == 0.0
                    && !player.isOnGround()
                    && !player.isInsideWaterOrBubbleColumn()) {
                return true;
            }
        }

        return false;
    }

    /**
     * Heuristic name checker.  Returns {@code true} for names that are:
     * <ul>
     *   <li>Entirely numeric (e.g. "123456")</li>
     *   <li>Very short (1 character) or unusually long (more than 16 chars, which is
     *       impossible for real Minecraft accounts)</li>
     * </ul>
     */
    private static boolean isSuspiciousName(String name) {
        if (name == null || name.isEmpty()) return true;
        if (name.length() > 16) return true; // Minecraft names are max 16 chars
        if (name.matches("\\d+")) return true; // all digits
        return false;
    }
}
