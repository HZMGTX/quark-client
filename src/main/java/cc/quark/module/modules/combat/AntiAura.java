package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.util.ChatUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class AntiAura extends Module {

    private final DoubleSetting range = register(new DoubleSetting("Range", "Detection range in blocks", 6.0, 1.0, 10.0));
    private final DoubleSetting angleThreshold = register(new DoubleSetting("Angle", "Max degrees aimed at you to trigger", 5.0, 1.0, 30.0));
    private final BoolSetting alert = register(new BoolSetting("Alert", "Send chat alert when targeted", true));
    private final BoolSetting jump = register(new BoolSetting("Jump", "Jump when targeted to break aura", true));

    private final List<String> targeting = new ArrayList<>();
    private boolean wasTargeted = false;

    public AntiAura() {
        super("AntiAura", "Detects when enemies aim at you and alerts/evades", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        targeting.clear();
        wasTargeted = false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        targeting.clear();

        Vec3d myPos = mc.player.getEyePos();

        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player) continue;
            if (!(entity instanceof PlayerEntity player)) continue;
            if (player.isDead()) continue;
            if (mc.player.distanceTo(player) > range.get()) continue;

            Vec3d lookVec = player.getRotationVec(1.0f).normalize();
            Vec3d toMe = myPos.subtract(player.getEyePos()).normalize();

            double dot = MathHelper.clamp(lookVec.dotProduct(toMe), -1.0, 1.0);
            double angle = Math.toDegrees(Math.acos(dot));

            if (angle <= angleThreshold.get()) {
                String name = player.getGameProfile().getName();
                targeting.add(name);
            }
        }

        boolean nowTargeted = !targeting.isEmpty();

        if (nowTargeted && !wasTargeted && alert.isEnabled()) {
            ChatUtil.warn("Targeted by: " + String.join(", ", targeting));
        }

        if (nowTargeted && jump.isEnabled() && mc.player.isOnGround()) {
            mc.player.jump();
        }

        wasTargeted = nowTargeted;
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (targeting.isEmpty()) return;
        DrawContext ctx = event.getDrawContext();
        int screenW = mc.getWindow().getScaledWidth();
        String text = "Targeted by: " + String.join(", ", targeting);
        int tw = mc.textRenderer.getWidth(text);
        ctx.drawTextWithShadow(mc.textRenderer, text, (screenW - tw) / 2, 4, 0xFFFF4444);
    }

    @Override
    public String getSuffix() {
        return targeting.isEmpty() ? "Safe" : targeting.size() + " targeting";
    }
}
