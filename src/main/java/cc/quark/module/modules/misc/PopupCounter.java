package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventChat;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;

import java.util.HashMap;
import java.util.Map;

public class PopupCounter extends Module {

    private final BoolSetting showSelf = register(new BoolSetting(
            "Show Self", "Count your own totem pops", true));
    private final BoolSetting announce = register(new BoolSetting(
            "Announce", "Announce in chat when you pop a totem", false));
    private final BoolSetting showHUD = register(new BoolSetting(
            "Show HUD", "Display pop counts on screen", true));

    private int selfPops = 0;
    private final Map<String, Integer> playerPops = new HashMap<>();

    public PopupCounter() {
        super("PopupCounter", "Tracks totem of undying pops for players", Category.MISC);
    }

    @Override
    public void onEnable() {
        selfPops = 0;
        playerPops.clear();
    }

    @Override
    public String getSuffix() {
        return "Self: " + selfPops;
    }

    @EventHandler
    public void onChat(EventChat event) {
        if (!event.isIncoming()) return;
        String msg = event.getMessage();
        // Detect totem pop patterns: "[player] used a Totem of Undying"
        if (msg.contains("used a Totem of Undying") || msg.contains("totem pop")) {
            String[] parts = msg.split(" ");
            if (parts.length > 0) {
                String name = parts[0].replaceAll("§.", "").replaceAll("[^a-zA-Z0-9_]", "");
                if (!name.isEmpty()) {
                    playerPops.merge(name, 1, Integer::sum);
                    if (mc.player != null && name.equalsIgnoreCase(mc.player.getName().getString())) {
                        selfPops++;
                        if (announce.isEnabled()) {
                            mc.player.sendMessage(
                                net.minecraft.text.Text.literal("§c[PopupCounter] §fYou popped a totem! §c(x" + selfPops + ")"),
                                false);
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (!showHUD.isEnabled() || mc.player == null) return;

        DrawContext ctx = event.getDrawContext();
        int y = 20;

        if (showSelf.isEnabled() && selfPops > 0) {
            RenderUtil.drawCustomText(ctx, "§cYour Pops: §f" + selfPops, 2, y, 0xFFFFFFFF);
            y += 10;
        }

        // Show top 5 players
        playerPops.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .limit(5)
            .forEach(e -> {});

        int rank = 1;
        for (var entry : playerPops.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(5).toList()) {
            String selfMark = (mc.player != null &&
                entry.getKey().equalsIgnoreCase(mc.player.getName().getString())) ? "§e" : "§f";
            RenderUtil.drawCustomText(ctx,
                "§7" + rank + ". " + selfMark + entry.getKey() + " §c" + entry.getValue() + "x",
                2, y, 0xFFFFFFFF);
            y += 10;
            rank++;
        }
    }
}
