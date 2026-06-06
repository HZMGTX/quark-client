package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.util.ColorUtil;
import net.minecraft.client.gui.DrawContext;

public class HitConfirm extends Module {
    private final IntSetting duration = register(new IntSetting("Duration", "Ticks to show hit confirm", 10, 5, 30));
    private int hitTimer = 0;
    private int lastHealth = 0;

    public HitConfirm() { super("HitConfirm", "Shows visual feedback when hitting an entity", Category.COMBAT); }

    @EventHandler
    public void onTick(EventTick e) {
        if (mc.player == null) return;
        if (hitTimer > 0) hitTimer--;
        // Detect when we hit something via tracking nearby entity health changes
        if (mc.targetedEntity instanceof net.minecraft.entity.LivingEntity le) {
            int hp = (int) le.getHealth();
            if (hp < lastHealth) hitTimer = duration.get();
            lastHealth = hp;
        } else {
            lastHealth = 0;
        }
    }

    @EventHandler
    public void onRender2D(EventRender2D e) {
        if (hitTimer <= 0 || mc.player == null) return;
        DrawContext ctx = e.getDrawContext();
        int sw = ctx.getScaledWindowWidth();
        int sh = ctx.getScaledWindowHeight();
        float alpha = (float) hitTimer / duration.get();
        ctx.fill(sw / 2 - 20, sh / 2 - 20, sw / 2 + 20, sh / 2 + 20,
                ColorUtil.withAlpha(0xFF0000, (int)(80 * alpha)));
    }
}
