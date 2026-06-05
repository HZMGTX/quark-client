package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.util.ColorUtil;
import net.minecraft.client.gui.DrawContext;

public class ComboCounter2 extends Module {
    private final BoolSetting rainbow = register(new BoolSetting("Rainbow", "Rainbow color combo counter", true));
    private int combo = 0;
    private int lastHurtTime = 0;
    private int lastTargetId = -1;

    public ComboCounter2() { super("ComboCounter2", "Tracks your hit combo with visual display", Category.COMBAT); }
    @Override public void onEnable() { mc.getEventBus().subscribe(this); combo = 0; }
    @Override public void onDisable() { mc.getEventBus().unsubscribe(this); }

    @EventHandler
    public void onTick(EventTick e) {
        if (mc.player == null || mc.world == null) return;
        for (var ent : mc.world.getEntities()) {
            if (!(ent instanceof net.minecraft.entity.LivingEntity le) || le == mc.player) continue;
            if (mc.player.distanceTo(le) > 4) continue;
            int hurtTime = le.hurtTime;
            if (hurtTime > lastHurtTime && ent.getId() == lastTargetId) combo++;
            else if (hurtTime > 0 && ent.getId() != lastTargetId) { combo = 1; lastTargetId = ent.getId(); }
            lastHurtTime = hurtTime;
        }
        if (mc.player.hurtTime > 0) combo = 0;
    }

    @EventHandler
    public void onRender2D(EventRender2D e) {
        if (combo <= 0) return;
        DrawContext ctx = e.getDrawContext();
        int sw = ctx.getScaledWindowWidth();
        int sh = ctx.getScaledWindowHeight();
        long t = System.currentTimeMillis();
        int color = rainbow.isEnabled() ? ColorUtil.rainbow((int)(t % 2000), 1f, 1f) : 0xFFFFAA00;
        String txt = combo + "x";
        cc.quark.util.RenderUtil.drawCustomText(ctx, txt, sw / 2 - mc.textRenderer.getWidth(txt) / 2, sh / 2 + 20, color);
    }
}
