package cc.quark.module.modules.staff;

import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.setting.StringSetting;
import cc.quark.util.ChatUtil;

public class HealthManager extends Module {

    private final StringSetting targetPlayer = register(new StringSetting(
            "Target Player", "Name of the player whose health to set", ""));
    private final IntSetting health = register(new IntSetting(
            "Health", "Health value to set (1–20 half-hearts)", 20, 1, 20));
    private final IntSetting absorption = register(new IntSetting(
            "Absorption", "Absorption hearts to grant (0 = none)", 0, 0, 20));

    public HealthManager() {
        super("HealthManager", "Sets or restores the health and absorption of a target player", Category.STAFF);
    }

    @Override
    public void onEnable() {
        if (mc.player == null) { disable(); return; }
        String target = targetPlayer.get().trim();
        if (target.isEmpty()) {
            ChatUtil.warn("[HealthManager] Set Target Player before enabling.");
            disable();
            return;
        }

        // Use /attribute command (vanilla 1.21+) to set max health then heal
        mc.player.networkHandler.sendChatCommand(
                "attribute " + target + " minecraft:generic.max_health base set " + health.get());
        mc.player.networkHandler.sendChatCommand(
                "effect give " + target + " minecraft:instant_health 1 255 true");

        if (absorption.get() > 0) {
            mc.player.networkHandler.sendChatCommand(
                    "effect give " + target + " minecraft:absorption 60 " + (absorption.get() - 1) + " true");
            ChatUtil.info("§6[HealthManager] §fSet health=§e" + health.get()
                    + " §fabsorption=§e" + absorption.get() + " §fon §e" + target);
        } else {
            ChatUtil.info("§6[HealthManager] §fSet health=§e" + health.get() + " §fon §e" + target);
        }
        disable();
    }
}
