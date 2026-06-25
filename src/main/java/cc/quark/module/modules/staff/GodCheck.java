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

public class GodCheck extends Module {

    private final BoolSetting alertOnly = register(new BoolSetting(
            "Alert Only", "Only alert instead of kicking the suspected player", true));
    private final BoolSetting checkNoHealthChange = register(new BoolSetting(
            "No Health Change", "Flag players whose health never decreases after hits", true));
    private final IntSetting flagsToAction = register(new IntSetting(
            "Flags To Action", "Consecutive suspicious ticks before taking action", 10, 3, 30));

    private final Map<String, Float> lastHealth = new HashMap<>();
    private final Map<String, Integer> noChangeStreak = new HashMap<>();
    private final Map<String, Boolean> alerted = new HashMap<>();

    public GodCheck() {
        super("GodCheck", "Detects players with god mode / impossible health", Category.STAFF, 0);
    }

    @Override
    public void onEnable() {
        if (mc.player == null) { disable(); return; }
        lastHealth.clear();
        noChangeStreak.clear();
        alerted.clear();
        ChatUtil.info("§6[GodCheck] §fMonitoring for god-mode players.");
    }

    @Override
    public void onDisable() {
        lastHealth.clear();
        noChangeStreak.clear();
        alerted.clear();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player == mc.player) continue;
            String name = player.getName().getString();
            float hp = player.getHealth();
            float maxHp = player.getMaxHealth();

            // Flag impossible health values (above vanilla max or negative)
            if (hp > maxHp + 0.1f || hp < 0) {
                triggerFlag(name, "impossible health §c(" + hp + "/" + maxHp + ")");
                continue;
            }

            // Flag players whose health is always at max even when they should be taking damage
            if (checkNoHealthChange.isEnabled()) {
                float prev = lastHealth.getOrDefault(name, hp);
                if (hp >= maxHp && prev >= maxHp) {
                    // They remain at full health; increment streak
                    int streak = noChangeStreak.getOrDefault(name, 0) + 1;
                    noChangeStreak.put(name, streak);
                    if (streak >= flagsToAction.get() * 20 && !alerted.getOrDefault(name, false)) {
                        triggerFlag(name, "always-full health streak");
                        alerted.put(name, true);
                    }
                } else {
                    noChangeStreak.put(name, 0);
                    alerted.put(name, false);
                }
            }

            lastHealth.put(name, hp);
        }
    }

    private void triggerFlag(String name, String reason) {
        if (alertOnly.isEnabled()) {
            ChatUtil.info("§6[GodCheck] §cFlagged §e" + name + " §7— " + reason);
        } else {
            ChatUtil.info("§6[GodCheck] §cKicking §e" + name + " §7— " + reason);
            if (mc.player != null) {
                mc.player.networkHandler.sendChatCommand("kick " + name + " Suspected god mode");
            }
        }
    }
}
