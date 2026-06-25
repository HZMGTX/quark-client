package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;

public class CombatInfo2 extends Module {
    private final IntSetting x = register(new IntSetting("X", "HUD X", 2, 0, 1920));
    private final IntSetting y = register(new IntSetting("Y", "HUD Y", 2, 0, 1080));
    private final BoolSetting showTarget = register(new BoolSetting("Target", "Show target info", true));
    private final BoolSetting showDPS = register(new BoolSetting("DPS", "Show damage per second", true));

    private PlayerEntity currentTarget;
    private float lastTargetHealth;
    private long lastDmgTime;
    private double dps;

    public CombatInfo2() {
        super("Combat Info+", "Advanced combat HUD with DPS tracking", Category.COMBAT, 0);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        // Find closest player as target
        PlayerEntity closest = null;
        double minDist = Double.MAX_VALUE;
        for (var p : mc.world.getPlayers()) {
            if (p == mc.player) continue;
            double d = p.squaredDistanceTo(mc.player);
            if (d < minDist) { minDist = d; closest = p; }
        }

        if (closest != null && closest == currentTarget) {
            float curHp = closest.getHealth();
            if (curHp < lastTargetHealth) {
                float dmg = lastTargetHealth - curHp;
                long now = System.currentTimeMillis();
                double timeDelta = (now - lastDmgTime) / 1000.0;
                if (timeDelta > 0 && timeDelta < 3.0) dps = dmg / timeDelta;
                lastDmgTime = now;
                lastTargetHealth = curHp;
            }
        } else {
            currentTarget = closest;
            if (closest != null) lastTargetHealth = closest.getHealth();
            dps = 0;
        }
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null) return;
        DrawContext ctx = event.getDrawContext();
        int yOff = 0;

        if (showTarget.isEnabled() && currentTarget != null) {
            ctx.drawText(mc.textRenderer, "§cTarget: §f" + currentTarget.getName().getString(), x.get(), y.get() + yOff, 0xFFFFFF, true);
            yOff += 10;
            ctx.drawText(mc.textRenderer, "§cHP: §f" + String.format("%.1f", currentTarget.getHealth()), x.get(), y.get() + yOff, 0xFFFFFF, true);
            yOff += 10;
        }
        if (showDPS.isEnabled() && dps > 0) {
            ctx.drawText(mc.textRenderer, "§eDPS: §f" + String.format("%.1f", dps), x.get(), y.get() + yOff, 0xFFFFFF, true);
        }
    }
}
