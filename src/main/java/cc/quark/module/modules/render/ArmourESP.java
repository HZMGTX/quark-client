package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;

/**
 * ArmourESP - Shows nearby players' armor pieces and optionally their durability.
 */
public class ArmourESP extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Display range in blocks", 16.0, 2.0, 64.0));
    private final BoolSetting showDurability = register(new BoolSetting(
            "Durability", "Show armor durability percentage", true));

    public ArmourESP() {
        super("ArmourESP", "Shows armor stats of nearby players", Category.RENDER);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null || mc.world == null) return;
        DrawContext ctx = event.getDrawContext();

        for (var entity : mc.world.getEntities()) {
            if (!(entity instanceof PlayerEntity player)) continue;
            if (player == mc.player) continue;
            if (mc.player.distanceTo(player) > range.get()) continue;

            double[] screen = projectToScreen(player.getPos().add(0, player.getHeight() + 0.2, 0));
            if (screen == null) continue;

            int x = (int)screen[0];
            int y = (int)screen[1];

            EquipmentSlot[] slots = {
                EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
            };
            String[] labels = {"H", "C", "L", "F"};

            for (int i = 0; i < slots.length; i++) {
                ItemStack stack = player.getEquippedStack(slots[i]);
                if (stack.isEmpty()) continue;

                String text = stack.getName().getString();
                if (showDurability.isEnabled() && stack.isDamageable()) {
                    int maxDur = stack.getMaxDamage();
                    int curDur = maxDur - stack.getDamage();
                    int pct = maxDur > 0 ? (int)(100f * curDur / maxDur) : 100;
                    text += " " + pct + "%";
                }

                int durColor = getDurabilityColor(stack);
                ctx.drawTextWithShadow(mc.textRenderer, labels[i] + ": " + text, x + 4, y + i * 9, durColor);
            }
        }
    }

    private int getDurabilityColor(ItemStack stack) {
        if (!stack.isDamageable()) return 0xFFFFFFFF;
        float pct = 1f - (float)stack.getDamage() / stack.getMaxDamage();
        int r = (int)((1f - pct) * 255);
        int g = (int)(pct * 255);
        return 0xFF000000 | (r << 16) | (g << 8);
    }

    private double[] projectToScreen(Vec3d worldPos) {
        try {
            var camera = mc.gameRenderer.getCamera();
            Vec3d cam = camera.getPos();
            double dx = worldPos.x - cam.x, dy = worldPos.y - cam.y, dz = worldPos.z - cam.z;
            float yaw = (float)Math.toRadians(camera.getYaw());
            float pitch = (float)Math.toRadians(camera.getPitch());
            double cosY = Math.cos(yaw), sinY = Math.sin(yaw);
            double cosP = Math.cos(pitch), sinP = Math.sin(pitch);
            double rx =  dx * cosY - dz * sinY;
            double ry = -dx * sinY * sinP + dy * cosP + dz * cosY * sinP;
            double rz =  dx * sinY * cosP + dy * sinP - dz * cosY * cosP;
            if (rz <= 0) return null;
            int sw = mc.getWindow().getScaledWidth(), sh = mc.getWindow().getScaledHeight();
            double fov = Math.toRadians(mc.options.getFov().getValue());
            return new double[]{
                    (rx / (rz * Math.tan(fov/2))) * (sw/2.0) + sw/2.0,
                    (-ry / (rz * Math.tan(fov/2))) * (sh/2.0) + sh/2.0
            };
        } catch (Exception e) { return null; }
    }
}
