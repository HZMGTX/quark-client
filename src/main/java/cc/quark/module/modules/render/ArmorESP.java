package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;

public class ArmorESP extends Module {

    private final BoolSetting showDurability = register(new BoolSetting("ShowDurability", "Show remaining durability percentage", true));

    public ArmorESP() {
        super("ArmorESP", "Renders armor icons and optionally durability above each player's head", Category.RENDER);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.world == null || mc.player == null) return;
        DrawContext ctx = event.getDrawContext();

        for (Entity e : mc.world.getEntities()) {
            if (!(e instanceof PlayerEntity p)) continue;
            if (e == mc.player) continue;

            Vec3d headPos = new Vec3d(p.getX(), p.getY() + p.getHeight() + 0.3, p.getZ());
            double[] screen = RenderUtil.project(headPos);
            if (screen == null) continue;

            EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
            int drawX = (int) screen[0] - (slots.length * 9) / 2;
            int drawY = (int) screen[1];

            for (EquipmentSlot slot : slots) {
                ItemStack stack = p.getEquippedStack(slot);
                if (stack.isEmpty()) { drawX += 9; continue; }

                if (showDurability.isEnabled() && stack.getItem() instanceof ArmorItem && stack.isDamageable()) {
                    int maxDmg = stack.getMaxDamage();
                    int dmg    = stack.getDamage();
                    float pct  = 1.0f - (float) dmg / maxDmg;
                    int col    = pct > 0.5f ? 0xFF33FF33 : (pct > 0.25f ? 0xFFFFAA00 : 0xFFFF3333);
                    String durStr = String.valueOf((int)(pct * 100)) + "%";
                    RenderUtil.drawCustomText(ctx, durStr, drawX, drawY, col);
                } else {
                    // Just indicate presence
                    RenderUtil.drawCustomText(ctx, "A", drawX, drawY, 0xFFCCCCCC);
                }
                drawX += 9;
            }
        }
    }
}
