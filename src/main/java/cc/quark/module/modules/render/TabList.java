package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.setting.ModeSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TabList extends Module {

    private final BoolSetting showHealth = register(new BoolSetting("Health", "Show player health", true));
    private final BoolSetting showPing   = register(new BoolSetting("Ping",   "Show colored ping value", true));
    private final BoolSetting sort       = register(new BoolSetting("Sort",   "Sort the player list", true));
    private final ModeSetting sortMode   = register(new ModeSetting("Sort Mode", "How to sort the list",
            "Ping", "Ping", "Health", "Name", "Team"));
    private final IntSetting posX        = register(new IntSetting("X", "X position", 10, 0, 4000));
    private final IntSetting posY        = register(new IntSetting("Y", "Y position", 10, 0, 4000));

    private static final int ENTRY_W = 120;
    private static final int ENTRY_H = 14;
    private static final int COLS     = 2;
    private static final int PAD      = 2;

    public TabList() {
        super("TabList", "Custom tab list overlay with ping and health", Category.RENDER);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.options == null || mc.getNetworkHandler() == null) return;
        if (!mc.options.playerListKey.isPressed()) return;

        List<PlayerListEntry> players = new ArrayList<>(mc.getNetworkHandler().getPlayerList());

        if (sort.isEnabled()) {
            Comparator<PlayerListEntry> cmp = switch (sortMode.get()) {
                case "Health" -> Comparator.comparingDouble(p -> {
                    if (mc.world == null) return 0.0;
                    var entity = mc.world.getPlayerByUuid(p.getProfile().getId());
                    return entity == null ? Double.MAX_VALUE : entity.getHealth();
                });
                case "Name"   -> Comparator.comparing(p -> p.getProfile().getName().toLowerCase());
                case "Team"   -> Comparator.comparing(p -> {
                    var team = p.getScoreboardTeam();
                    return team == null ? "" : team.getName();
                });
                default       -> Comparator.comparingInt(PlayerListEntry::getLatency);
            };
            players.sort(cmp);
        }

        DrawContext ctx = event.getDrawContext();
        int startX = posX.get();
        int startY = posY.get();

        int totalRows = (int) Math.ceil((double) players.size() / COLS);
        int bgW = COLS * ENTRY_W + PAD;
        int bgH = totalRows * ENTRY_H + PAD * 2;
        ctx.fill(startX - PAD, startY - PAD, startX + bgW, startY + bgH, 0xAA0A0A0A);

        for (int i = 0; i < players.size(); i++) {
            PlayerListEntry entry = players.get(i);
            int col = i % COLS;
            int row = i / COLS;
            int ex = startX + col * ENTRY_W;
            int ey = startY + row * ENTRY_H;

            String name = entry.getProfile().getName();
            int nameColor = 0xFFFFFFFF;

            if (mc.player != null && name.equals(mc.player.getName().getString())) {
                nameColor = 0xFFFFFF55;
            }

            ctx.drawTextWithShadow(mc.textRenderer, name, ex, ey + 2, nameColor);

            int textRight = ex + ENTRY_W - 2;

            if (showPing.isEnabled()) {
                int ping = entry.getLatency();
                int pingColor = pingColor(ping);
                String pingStr = ping + "ms";
                int pingW = mc.textRenderer.getWidth(pingStr);
                ctx.drawTextWithShadow(mc.textRenderer, pingStr, textRight - pingW, ey + 2, pingColor);
                textRight -= pingW + 2;
            }

            if (showHealth.isEnabled() && mc.world != null) {
                var entity = mc.world.getPlayerByUuid(entry.getProfile().getId());
                if (entity != null) {
                    float health = entity.getHealth();
                    float maxHealth = entity.getMaxHealth();
                    float pct = maxHealth > 0 ? health / maxHealth : 0f;
                    int healthColor = healthColor(pct);
                    String healthStr = String.format("%.0f", health);
                    int hw = mc.textRenderer.getWidth(healthStr);
                    ctx.drawTextWithShadow(mc.textRenderer, healthStr, textRight - hw, ey + 2, healthColor);
                }
            }
        }
    }

    private int pingColor(int ping) {
        if (ping < 50)  return 0xFF55FF55;
        if (ping < 100) return 0xFFFFFF55;
        if (ping < 200) return 0xFFFFAA00;
        return 0xFFFF5555;
    }

    private int healthColor(float pct) {
        if (pct > 0.6f) return 0xFF55FF55;
        if (pct > 0.3f) return 0xFFFFFF55;
        return 0xFFFF5555;
    }
}
