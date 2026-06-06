package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventAttack;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

public class TargetHUD extends Module {

    private final BoolSetting showArmor = register(new BoolSetting("ShowArmor", "Show target armor value", true));
    private final BoolSetting showDist  = register(new BoolSetting("ShowDist",  "Show distance to target", true));
    private final IntSetting x = register(new IntSetting("X", "HUD X position", 10, 0, 3840));
    private final IntSetting y = register(new IntSetting("Y", "HUD Y position", 60, 0, 2160));

    private LivingEntity target;
    private long lastAttackMs;

    public TargetHUD() {
        super("TargetHUD", "Shows target player health, distance and armor as a HUD element", Category.RENDER);
    }

    @EventHandler
    public void onAttack(EventAttack event) {
        if (event.getTarget() instanceof LivingEntity le) {
            target = le;
            lastAttackMs = System.currentTimeMillis();
        }
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null || mc.world == null) return;

        // Clear stale target after 3 s of no attacks
        if (target != null && (System.currentTimeMillis() - lastAttackMs > 3000
                || target.isRemoved() || !target.isAlive())) {
            target = null;
        }
        if (target == null) return;

        DrawContext ctx = event.getDrawContext();
        int px = x.get(), py = y.get();
        int w = 130, h = 36;

        // Background
        ctx.fill(px, py, px + w, py + h, 0xBB181818);
        ctx.fill(px, py, px + w, py + 1, 0xFF4466FF);

        // Name
        String name = target.getName().getString();
        ctx.drawTextWithShadow(mc.textRenderer, name, px + 4, py + 4, 0xFFFFFFFF);

        // Health bar
        float hp    = target.getHealth();
        float maxHp = target.getMaxHealth();
        float pct   = MathHelper.clamp(hp / maxHp, 0f, 1f);
        int barW = w - 8;
        ctx.fill(px + 4, py + 16, px + 4 + barW, py + 22, 0xFF333333);
        int fillColor = pct > 0.6f ? 0xFF00FF44 : pct > 0.3f ? 0xFFFFFF00 : 0xFFFF2222;
        ctx.fill(px + 4, py + 16, px + 4 + (int)(barW * pct), py + 22, fillColor);

        // HP text
        String hpStr = String.format("%.1f HP", hp / 2f);
        ctx.drawTextWithShadow(mc.textRenderer, hpStr, px + 4, py + 24, fillColor);

        // Optional: armor
        if (showArmor.isEnabled()) {
            int armor = target.getArmor();
            String armorStr = "ARM:" + armor;
            int armorX = px + w - mc.textRenderer.getWidth(armorStr) - 4;
            ctx.drawTextWithShadow(mc.textRenderer, armorStr, armorX, py + 24, 0xFFAAAAAA);
        }

        // Optional: distance
        if (showDist.isEnabled()) {
            double dist = mc.player.distanceTo(target);
            String distStr = String.format("%.1fm", dist);
            int distX = px + w - mc.textRenderer.getWidth(distStr) - 4;
            ctx.drawTextWithShadow(mc.textRenderer, distStr, distX, py + 4, 0xFFCCCCCC);
        }
    }
}
