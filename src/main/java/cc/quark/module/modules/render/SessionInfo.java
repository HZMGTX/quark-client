package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventAttack;
import cc.quark.event.events.EventRender2D;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.LivingEntity;

import java.util.ArrayDeque;
import java.util.Deque;

public class SessionInfo extends Module {

    private final IntSetting x = register(new IntSetting("X", "X pos", 4, 0, 500));
    private final IntSetting y = register(new IntSetting("Y", "Y pos", 54, 0, 500));

    private final BoolSetting showTime   = register(new BoolSetting("Time",           "Show time played this session", true));
    private final BoolSetting showKills  = register(new BoolSetting("Kills",          "Show kill count",                true));
    private final BoolSetting showDeaths = register(new BoolSetting("Deaths",         "Show death count",               true));
    private final BoolSetting showBps    = register(new BoolSetting("BPS",            "Show current blocks per second", true));
    private final BoolSetting showBlocks = register(new BoolSetting("Blocks Broken",  "Show blocks broken count",       false));

    private long sessionStart = System.currentTimeMillis();
    private int  kills        = 0;
    private int  deaths       = 0;
    private int  blocksBroken = 0;
    private boolean wasAlive  = true;
    private float lastHealth  = -1f;

    private final Deque<Double> bpsHistory = new ArrayDeque<>();
    private double prevX, prevZ;
    private double bps;

    public SessionInfo() {
        super("SessionInfo", "Displays session statistics and player info", Category.RENDER);
    }

    @Override
    public void onEnable() {
        sessionStart = System.currentTimeMillis();
        kills = 0;
        deaths = 0;
        blocksBroken = 0;
        wasAlive = true;
        lastHealth = -1f;
        bpsHistory.clear();
        if (mc.player != null) {
            prevX = mc.player.getX();
            prevZ = mc.player.getZ();
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        double dx = mc.player.getX() - prevX;
        double dz = mc.player.getZ() - prevZ;
        double speed = Math.sqrt(dx * dx + dz * dz) * 20.0;
        prevX = mc.player.getX();
        prevZ = mc.player.getZ();

        bpsHistory.addLast(speed);
        while (bpsHistory.size() > 5) bpsHistory.pollFirst();
        bps = bpsHistory.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);

        float currentHealth = mc.player.getHealth();
        if (lastHealth > 0 && currentHealth <= 0 && wasAlive) {
            deaths++;
            wasAlive = false;
        } else if (currentHealth > 0) {
            wasAlive = true;
        }
        lastHealth = currentHealth;
    }

    @EventHandler
    public void onAttack(EventAttack event) {
        if (event.getTarget() instanceof LivingEntity living) {
            if (living.getHealth() - 1f <= 0) {
                kills++;
            }
        }
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.getSession() == null) return;
        DrawContext ctx = event.getDrawContext();

        int rx = x.get();
        int ry = y.get();
        int lineH = mc.textRenderer.fontHeight + 2;
        int pad = 3;

        String user = mc.getSession().getUsername();
        ctx.drawTextWithShadow(mc.textRenderer, "§7User: §f" + user, rx, ry, 0xFFFFFFFF);
        ry += lineH;

        if (showTime.isEnabled()) {
            long elapsed = System.currentTimeMillis() - sessionStart;
            long secs    = elapsed / 1000;
            long mins    = secs / 60;
            long hrs     = mins / 60;
            String timeStr = hrs > 0
                    ? String.format("%dh %02dm %02ds", hrs, mins % 60, secs % 60)
                    : String.format("%dm %02ds", mins, secs % 60);
            ctx.drawTextWithShadow(mc.textRenderer, "§7Time: §f" + timeStr, rx, ry, 0xFFFFFFFF);
            ry += lineH;
        }

        if (showKills.isEnabled()) {
            ctx.drawTextWithShadow(mc.textRenderer, "§7Kills: §a" + kills, rx, ry, 0xFFFFFFFF);
            ry += lineH;
        }

        if (showDeaths.isEnabled()) {
            ctx.drawTextWithShadow(mc.textRenderer, "§7Deaths: §c" + deaths, rx, ry, 0xFFFFFFFF);
            ry += lineH;
        }

        if (showBps.isEnabled()) {
            ctx.drawTextWithShadow(mc.textRenderer,
                    String.format("§7BPS: §f%.2f", bps), rx, ry, 0xFFFFFFFF);
            ry += lineH;
        }

        if (showBlocks.isEnabled()) {
            ctx.drawTextWithShadow(mc.textRenderer, "§7Broken: §f" + blocksBroken, rx, ry, 0xFFFFFFFF);
        }
    }
}
