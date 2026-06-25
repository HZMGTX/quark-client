package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventDamage;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import net.minecraft.client.gui.DrawContext;

public class CombatStats extends Module {

    private final IntSetting posX = register(new IntSetting("X", "HUD X position", 4, 0, 3000));
    private final IntSetting posY = register(new IntSetting("Y", "HUD Y position", 4, 0, 3000));

    private float totalDamageTaken;
    private int   hitsTaken;
    private long  sessionStart;
    private float prevHealth;

    public CombatStats() {
        super("CombatStats", "Tracks and displays damage taken, hits taken, and DPS received this session", Category.RENDER);
    }

    @Override
    public void onEnable() {
        totalDamageTaken = 0;
        hitsTaken = 0;
        sessionStart = System.currentTimeMillis();
        prevHealth = mc.player != null ? mc.player.getHealth() : 20f;
    }

    @EventHandler
    public void onDamage(EventDamage event) {
        if (mc.player == null) return;
        totalDamageTaken += event.getAmount();
        hitsTaken++;
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null) return;
        DrawContext ctx = event.getDrawContext();
        int x = posX.get(), y = posY.get();
        int lh = mc.textRenderer.fontHeight + 2;

        long elapsed = Math.max(1, System.currentTimeMillis() - sessionStart);
        double elapsedSec = elapsed / 1000.0;
        double dps = totalDamageTaken / elapsedSec;

        ctx.drawTextWithShadow(mc.textRenderer, String.format("DPS rcvd: %.2f", dps), x, y, 0xFFFF5555);
        y += lh;
        ctx.drawTextWithShadow(mc.textRenderer, "Hits taken: " + hitsTaken, x, y, 0xFFFFAA00);
        y += lh;
        ctx.drawTextWithShadow(mc.textRenderer, String.format("Total dmg: %.1f", totalDamageTaken), x, y, 0xFFFF8888);
        y += lh;
        float hp = mc.player.getHealth();
        float maxHp = mc.player.getMaxHealth();
        ctx.drawTextWithShadow(mc.textRenderer, String.format("HP: %.1f/%.1f", hp, maxHp), x, y, 0xFF55FF55);
    }
}
