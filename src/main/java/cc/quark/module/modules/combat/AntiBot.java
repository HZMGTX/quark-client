package cc.quark.module.modules.combat;

import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

public class AntiBot extends Module {

    private final BoolSetting nameFilter = register(new BoolSetting(
            "Name Filter", "Flag entities with suspicious names (all digits, invalid length)", true));

    private final BoolSetting ageFilter = register(new BoolSetting(
            "Age Filter", "Flag entities that are abnormally young (< 20 ticks old)", true));

    private static AntiBot instance;

    public AntiBot() {
        super("AntiBot", "Filters bot entities from combat module target lists", Category.COMBAT);
        instance = this;
    }

    public static boolean isBot(Entity entity) {
        if (instance == null || !instance.isEnabled()) return false;
        if (!(entity instanceof PlayerEntity player)) return false;

        if (instance.nameFilter.isEnabled()) {
            String name = player.getGameProfile().getName();
            if (isSuspiciousName(name)) return true;
        }

        // Ping check: entity not present in server player list is almost certainly synthetic
        var mc = net.minecraft.client.MinecraftClient.getInstance();
        if (mc.getNetworkHandler() != null) {
            boolean inList = mc.getNetworkHandler()
                    .getPlayerList()
                    .stream()
                    .anyMatch(e -> e.getProfile().getId().equals(player.getGameProfile().getId()));
            if (!inList) return true;
        }

        if (instance.ageFilter.isEnabled()) {
            // Entity age < 20 ticks suggests it was just spawned in by anti-cheat
            if (player.age < 20) return true;
        }

        return false;
    }

    private static boolean isSuspiciousName(String name) {
        if (name == null || name.isEmpty()) return true;
        if (name.length() > 16) return true;
        if (name.matches("\\d+")) return true;
        return false;
    }
}
