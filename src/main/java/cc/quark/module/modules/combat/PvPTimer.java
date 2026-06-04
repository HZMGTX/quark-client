package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventDamage;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.client.gui.DrawContext;

public class PvPTimer extends Module {

    private final BoolSetting showHUD = register(new BoolSetting("Show HUD", "Display timer on screen", true));
    private final IntSetting combatTime = register(new IntSetting("Combat Time", "Seconds before out of combat", 15, 1, 60));

    private long lastDamageTime = 0L;
    private boolean inCombat = false;

    public PvPTimer() {
        super("PvPTimer", "Tracks PvP combat timer, warns when out of combat", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        lastDamageTime = 0L;
        inCombat = false;
    }

    @EventHandler
    public void onDamage(EventDamage event) {
        if (mc.player == null) return;
        lastDamageTime = System.currentTimeMillis();
        inCombat = true;
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (!showHUD.isEnabled() || mc.player == null) return;

        DrawContext ctx = event.getDrawContext();
        long elapsed = System.currentTimeMillis() - lastDamageTime;
        long threshold = combatTime.get() * 1000L;

        if (lastDamageTime == 0L) {
            ctx.drawTextWithShadow(mc.textRenderer, "PvP: Idle", 4, 4, 0xFFAAAAAA);
            return;
        }

        if (elapsed >= threshold) {
            inCombat = false;
            ctx.drawTextWithShadow(mc.textRenderer, "PvP: Out of combat", 4, 4, 0xFF55FF55);
        } else {
            long remaining = (threshold - elapsed) / 1000L;
            inCombat = true;
            ctx.drawTextWithShadow(mc.textRenderer, "PvP: " + remaining + "s", 4, 4, 0xFFFF5555);
        }
    }

    public boolean isInCombat() {
        return inCombat;
    }
}
