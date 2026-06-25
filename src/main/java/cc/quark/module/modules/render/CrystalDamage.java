package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;

public class CrystalDamage extends Module {
    private final IntSetting x = register(new IntSetting("X", "HUD X", 10, 0, 1920));
    private final IntSetting y = register(new IntSetting("Y", "HUD Y", 120, 0, 1080));
    private final BoolSetting showSelfDamage = register(new BoolSetting("Self Damage", "Show self crystal damage", true));

    public CrystalDamage() {
        super("Crystal Damage", "Shows crystal damage predictions on HUD", Category.RENDER, 0);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null || mc.world == null) return;
        DrawContext ctx = event.getDrawContext();

        EndCrystalEntity nearestCrystal = null;
        PlayerEntity nearestEnemy = null;
        double minDist = Double.MAX_VALUE;

        for (var entity : mc.world.getEntities()) {
            if (entity instanceof EndCrystalEntity c) {
                double d = c.squaredDistanceTo(mc.player);
                if (d < minDist) { minDist = d; nearestCrystal = c; }
            }
            if (entity instanceof PlayerEntity p && p != mc.player) {
                nearestEnemy = p;
            }
        }

        if (nearestCrystal != null && nearestEnemy != null) {
            double dist = nearestCrystal.distanceTo(nearestEnemy);
            double est = Math.max(0, 12.0 - dist * 1.5);
            ctx.drawText(mc.textRenderer, "§cCrystal DMG: §f" + String.format("%.1f", est), x.get(), y.get(), 0xFFFFFF, true);
        }

        if (showSelfDamage.isEnabled() && nearestCrystal != null) {
            double selfDist = nearestCrystal.distanceTo(mc.player);
            double selfEst = Math.max(0, 12.0 - selfDist * 1.5);
            ctx.drawText(mc.textRenderer, "§eSelf DMG: §f" + String.format("%.1f", selfEst), x.get(), y.get() + 12, 0xFFFFFF, true);
        }
    }
}
