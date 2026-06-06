package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventDamage;
import cc.quark.event.events.EventRender2D;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.damage.DamageTypes;

public class SessionInfo extends Module {

    private final BoolSetting showKD  = register(new BoolSetting("K/D",    "Show kill/death ratio",   true));
    private final BoolSetting showDmg = register(new BoolSetting("Damage", "Show total damage dealt",  true));

    private long  sessionStart = 0;
    private int   kills        = 0;
    private int   deaths       = 0;
    private float damageDealt  = 0f;
    private float prevHealth   = -1f;

    public SessionInfo() {
        super("SessionInfo", "HUD displaying session stats: kills, deaths, damage dealt, time played", Category.MISC);
    }

    @Override
    public void onEnable() {
        sessionStart = System.currentTimeMillis();
        kills = deaths = 0;
        damageDealt = 0f;
        prevHealth  = -1f;
    }

    @EventHandler
    public void onTick(EventTick e) {
        if (mc.player == null) return;
        float hp = mc.player.getHealth();
        if (prevHealth > 0 && hp <= 0) deaths++;
        if (prevHealth <= 0 && hp > 0 && deaths > 0) kills++;
        prevHealth = hp;
    }

    @EventHandler
    public void onDamage(EventDamage e) {
        if (mc.player == null) return;
        if (!e.getSource().isIn(DamageTypes.BYPASSES_ARMOR)) {
            damageDealt += e.getAmount();
        }
    }

    @EventHandler
    public void onRender2D(EventRender2D e) {
        if (mc.player == null) return;
        DrawContext ctx = e.getDrawContext();
        long elapsed = (System.currentTimeMillis() - sessionStart) / 1000;
        String time = String.format("%02d:%02d:%02d", elapsed / 3600, (elapsed % 3600) / 60, elapsed % 60);
        int x = 2, y = 2;
        RenderUtil.drawCustomText(ctx, "Time: " + time, x, y, 0xFFAAAAAA);
        y += 10;
        if (showKD.isEnabled()) {
            float kd = deaths == 0 ? kills : (float) kills / deaths;
            RenderUtil.drawCustomText(ctx, "K/D: " + kills + "/" + deaths + " (" + String.format("%.2f", kd) + ")", x, y, 0xFFAAAAAA);
            y += 10;
        }
        if (showDmg.isEnabled()) {
            RenderUtil.drawCustomText(ctx, "DMG: " + String.format("%.1f", damageDealt), x, y, 0xFFAAAAAA);
        }
    }
}
