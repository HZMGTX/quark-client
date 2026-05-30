package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public class PlayerInfo extends Module {

    private final IntSetting xPos = register(new IntSetting("X", "Panel X position", 10, 0, 500));
    private final IntSetting yPos = register(new IntSetting("Y", "Panel Y position", 200, 0, 500));
    private final BoolSetting showPing = register(new BoolSetting("ShowPing", "Display player ping", true));
    private final BoolSetting showArmor = register(new BoolSetting("ShowArmor", "Display armor value", true));

    public PlayerInfo() {
        super("PlayerInfo", "Shows info box for the player currently under the crosshair", Category.RENDER);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null || mc.world == null) return;
        if (!(mc.targetedEntity instanceof PlayerEntity target)) return;
        if (target == mc.player) return;

        DrawContext ctx = event.getDrawContext();
        int px = xPos.get();
        int py = yPos.get();
        int panelW = 120;
        int lineH = 10;
        int lines = 2;
        if (showPing.isEnabled()) lines++;
        if (showArmor.isEnabled()) lines++;

        int panelH = lines * lineH + 6;

        // Background
        ctx.fill(px, py, px + panelW, py + panelH, 0xAA000000);
        ctx.drawBorder(px, py, panelW, panelH, 0xFF555555);

        int ty = py + 3;

        // Name
        String name = target.getGameProfile().getName();
        ctx.drawTextWithShadow(mc.textRenderer, name, px + 3, ty, 0xFFFFFFFF);
        ty += lineH;

        // Health bar
        float hp = target.getHealth();
        float maxHp = target.getMaxHealth();
        float pct = maxHp > 0 ? hp / maxHp : 0;
        int barW = panelW - 6;
        int filledW = (int)(barW * pct);

        ctx.fill(px + 3, ty, px + 3 + barW, ty + 6, 0xFF333333);
        int hpColor = interpolateHealth(pct);
        ctx.fill(px + 3, ty, px + 3 + filledW, ty + 6, hpColor);
        ctx.drawTextWithShadow(mc.textRenderer, String.format("%.1f/%.0f", hp, maxHp), px + 3, ty, 0xFFAAAAAA);
        ty += lineH;

        // Armor value
        if (showArmor.isEnabled()) {
            int armorValue = target.getArmor();
            ctx.drawTextWithShadow(mc.textRenderer, "Armor: " + armorValue, px + 3, ty, 0xFF88CCFF);
            ty += lineH;
        }

        // Ping
        if (showPing.isEnabled()) {
            int ping = getPing(target);
            String pingStr = "Ping: " + (ping >= 0 ? ping + "ms" : "?");
            int pingColor = ping < 80 ? 0xFF00FF00 : (ping < 200 ? 0xFFFFFF00 : 0xFFFF4444);
            ctx.drawTextWithShadow(mc.textRenderer, pingStr, px + 3, ty, pingColor);
        }
    }

    private int getPing(PlayerEntity player) {
        if (mc.getNetworkHandler() == null) return -1;
        PlayerListEntry entry = mc.getNetworkHandler().getPlayerListEntry(player.getUuid());
        return entry != null ? entry.getLatency() : -1;
    }

    private int interpolateHealth(float pct) {
        // Green -> Yellow -> Red
        if (pct > 0.5f) {
            float t = (pct - 0.5f) * 2.0f;
            int r = (int)(255 * (1 - t));
            return 0xFF000000 | (r << 16) | (0xFF << 8);
        } else {
            float t = pct * 2.0f;
            int g = (int)(255 * t);
            return 0xFF000000 | (0xFF << 16) | (g << 8);
        }
    }
}
