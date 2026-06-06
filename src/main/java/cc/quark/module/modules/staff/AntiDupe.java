package cc.quark.module.modules.staff;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.ChatUtil;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.player.PlayerEntity;

import java.util.HashMap;
import java.util.Map;

public class AntiDupe extends Module {

    private final BoolSetting alertOnly = register(new BoolSetting(
            "Alert Only", "Only alert instead of kicking the player", true));
    private final BoolSetting watchInventory = register(new BoolSetting(
            "Watch Inventory", "Flag players whose item counts spike suspiciously", true));
    private final IntSetting threshold = register(new IntSetting(
            "Threshold", "Number of flagged ticks before action is taken", 5, 1, 20));

    private final Map<String, Integer> flagCount = new HashMap<>();
    private final Map<String, Integer> prevItemCount = new HashMap<>();

    public AntiDupe() {
        super("AntiDupe", "Detects and blocks item duplication exploits", Category.STAFF, 0);
    }

    @Override
    public void onEnable() {
        if (mc.player == null || mc.getNetworkHandler() == null) { disable(); return; }
        flagCount.clear();
        prevItemCount.clear();
        ChatUtil.info("§6[AntiDupe] §fMonitoring for dupe exploits.");
    }

    @Override
    public void onDisable() {
        flagCount.clear();
        prevItemCount.clear();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.getNetworkHandler() == null) return;
        if (!watchInventory.isEnabled()) return;

        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player == mc.player) continue;
            String name = player.getName().getString();

            int currentCount = 0;
            for (int i = 0; i < player.getInventory().size(); i++) {
                if (!player.getInventory().getStack(i).isEmpty()) {
                    currentCount += player.getInventory().getStack(i).getCount();
                }
            }

            if (prevItemCount.containsKey(name)) {
                int prev = prevItemCount.get(name);
                // Flag a sudden large increase that isn't explained by normal gameplay
                if (currentCount - prev > 256) {
                    int flags = flagCount.getOrDefault(name, 0) + 1;
                    flagCount.put(name, flags);
                    ChatUtil.info("§6[AntiDupe] §eFlag §f" + flags + "/" + threshold.get() + " §7on §e" + name
                            + " §7(items: §c+" + (currentCount - prev) + "§7)");

                    if (flags >= threshold.get()) {
                        flagCount.put(name, 0);
                        if (alertOnly.isEnabled()) {
                            ChatUtil.info("§6[AntiDupe] §cPossible dupe detected on §e" + name + "§c! (alert-only mode)");
                        } else {
                            ChatUtil.info("§6[AntiDupe] §cKicking §e" + name + " §cfor suspected item duplication.");
                            mc.player.networkHandler.sendChatCommand("kick " + name + " Suspected item duplication");
                        }
                    }
                } else if (currentCount < prev) {
                    // Reset flags if items decreased (normal usage)
                    flagCount.put(name, 0);
                }
            }

            prevItemCount.put(name, currentCount);
        }
    }
}
