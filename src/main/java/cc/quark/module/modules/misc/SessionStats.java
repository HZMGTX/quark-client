package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventAttack;
import cc.quark.event.events.EventDamage;
import cc.quark.event.events.EventRender2D;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;

public class SessionStats extends Module {

    private final BoolSetting showHUD  = register(new BoolSetting("Show HUD", "Display stats overlay", true));
    private final IntSetting posX      = register(new IntSetting("X", "HUD X position", 4, 0, 3000));
    private final IntSetting posY      = register(new IntSetting("Y", "HUD Y position", 80, 0, 3000));

    private long sessionStart;
    private int kills;
    private int deaths;
    private int blocksBroken;
    private float lastHealth;
    private boolean wasAlive;

    public SessionStats() {
        super("SessionStats", "Displays session statistics: playtime, blocks broken, kills and deaths", Category.MISC);
    }

    @Override
    public void onEnable() {
        sessionStart = System.currentTimeMillis();
        kills = 0;
        deaths = 0;
        blocksBroken = 0;
        wasAlive = true;
        lastHealth = mc.player != null ? mc.player.getHealth() : 20f;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        float health = mc.player.getHealth();
        if (lastHealth > 0 && health <= 0 && wasAlive) {
            deaths++;
            wasAlive = false;
        } else if (health > 0) {
            wasAlive = true;
        }
        lastHealth = health;
    }

    @EventHandler
    public void onAttack(EventAttack event) {
        if (event.getTarget() instanceof LivingEntity living) {
            if (living.getHealth() <= 1f) kills++;
        }
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (!showHUD.isEnabled()) return;
        if (mc.player == null) return;

        DrawContext ctx = event.getDrawContext();
        int x = posX.get();
        int y = posY.get();
        int lh = mc.textRenderer.fontHeight + 2;

        long elapsed = System.currentTimeMillis() - sessionStart;
        long secs = elapsed / 1000;
        long mins = secs / 60;
        long hrs  = mins / 60;
        String timeStr = hrs > 0
                ? String.format("%dh %02dm %02ds", hrs, mins % 60, secs % 60)
                : String.format("%dm %02ds", mins, secs % 60);

        ctx.drawTextWithShadow(mc.textRenderer, "§7Time: §f" + timeStr,  x, y, 0xFFFFFFFF); y += lh;
        ctx.drawTextWithShadow(mc.textRenderer, "§7Kills: §a" + kills,   x, y, 0xFFFFFFFF); y += lh;
        ctx.drawTextWithShadow(mc.textRenderer, "§7Deaths: §c" + deaths, x, y, 0xFFFFFFFF); y += lh;
        ctx.drawTextWithShadow(mc.textRenderer, "§7Broken: §f" + blocksBroken, x, y, 0xFFFFFFFF);
    }
}
