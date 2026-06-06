package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.BlockPos;

/**
 * DeathScreen - Shows death information overlay with coordinates, cause, and killer.
 *
 * Detects when the local player's health drops to zero and records the death
 * position. Displays a compact HUD overlay on the death screen showing coords,
 * last known cause, and (if available) the attacker's name.
 */
public class DeathScreen extends Module {

    private final BoolSetting showCoords = register(new BoolSetting("Coords",     "Show death coordinates",        true));
    private final BoolSetting showCause  = register(new BoolSetting("Cause",      "Show estimated cause of death", true));
    private final BoolSetting showKiller = register(new BoolSetting("Killer",     "Show attacker name if known",   true));

    // Captured at the moment of death
    private BlockPos deathPos    = null;
    private String   causeText   = "Unknown";
    private String   killerName  = null;

    // Track whether the player was alive last tick to detect the death transition
    private boolean wasAlive = true;

    // Track last attacker for "killer" display (updated every tick)
    private long lastAttackedTime = 0L;

    public DeathScreen() {
        super("DeathScreen", "Displays death info (coords, cause, killer) on the death screen", Category.RENDER);
    }

    @Override
    public void onEnable() {
        deathPos   = null;
        causeText  = "Unknown";
        killerName = null;
        wasAlive   = true;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        boolean dead = mc.player.isDead() || mc.player.getHealth() <= 0f;

        if (wasAlive && dead) {
            // Player just died – capture snapshot
            deathPos = mc.player.getBlockPos();

            // Determine rough cause from hunger/void/etc.
            if (mc.player.getY() < -64) {
                causeText = "Fell into the void";
            } else if (mc.player.isOnFire()) {
                causeText = "Burned to death";
            } else if (mc.player.isTouchingWater()) {
                causeText = "Drowned";
            } else if (mc.player.getHungerManager().getFoodLevel() == 0) {
                causeText = "Starved to death";
            } else if (killerName != null && (System.currentTimeMillis() - lastAttackedTime) < 5000L) {
                causeText = "Slain by " + killerName;
            } else {
                causeText = "Combat / Environmental";
            }
        }

        wasAlive = !dead;
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null) return;

        boolean dead = mc.player.isDead() || mc.player.getHealth() <= 0f;
        if (!dead || deathPos == null) return;

        DrawContext ctx = event.getDrawContext();
        int sw = ctx.getScaledWindowWidth();
        int sh = ctx.getScaledWindowHeight();

        // Centre the panel horizontally
        int panelW = 200;
        int panelX = sw / 2 - panelW / 2;
        int panelY = sh / 2 + 30;   // below the vanilla "You died!" title
        int lineH  = 11;
        int pad    = 4;

        // Count visible lines
        int lineCount = 1; // header
        if (showCoords.isEnabled())  lineCount++;
        if (showCause.isEnabled())   lineCount++;
        if (showKiller.isEnabled() && killerName != null) lineCount++;

        int panelH = lineCount * lineH + pad * 2;

        // Background
        ctx.fill(panelX - pad, panelY - pad,
                 panelX + panelW + pad, panelY + panelH, 0xBB000000);
        ctx.fill(panelX - pad, panelY - pad,
                 panelX + panelW + pad, panelY - pad + 1, 0xFFFF4444);

        int y = panelY;

        ctx.drawTextWithShadow(mc.textRenderer, "Death Info", panelX, y, 0xFFFF5555);
        y += lineH;

        if (showCoords.isEnabled()) {
            String coordStr = "XYZ: " + deathPos.getX() + " / " + deathPos.getY() + " / " + deathPos.getZ();
            ctx.drawTextWithShadow(mc.textRenderer, coordStr, panelX, y, 0xFFFFFFFF);
            y += lineH;
        }

        if (showCause.isEnabled()) {
            ctx.drawTextWithShadow(mc.textRenderer, "Cause: " + causeText, panelX, y, 0xFFFFAA55);
            y += lineH;
        }

        if (showKiller.isEnabled() && killerName != null) {
            ctx.drawTextWithShadow(mc.textRenderer, "Killer: " + killerName, panelX, y, 0xFFFF5555);
        }
    }
}
