package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender3D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.ColorSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.RenderUtil;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

/**
 * FishHook — renders a visible ESP box around the player's fishing bobber
 * and draws a dotted trajectory line from the player's position to the bobber.
 * Also shows when the bobber is in water (bite-ready state).
 */
public class FishHook extends Module {

    private final BoolSetting showBox = register(new BoolSetting(
            "Box", "Draw box around the fishing bobber", true));

    private final BoolSetting showLine = register(new BoolSetting(
            "Line", "Draw a line from player to bobber", true));

    private final BoolSetting showBiteAlert = register(new BoolSetting(
            "Bite Alert", "Color changes when fish may bite", true));

    private final ColorSetting normalColor = register(new ColorSetting(
            "Normal Color", "Color when bobber is not biting", 0xFF55AAFF));

    private final ColorSetting biteColor = register(new ColorSetting(
            "Bite Color", "Color when fish is biting (hook is in water)", 0xFFFF5555));

    private final DoubleSetting lineWidth = register(new DoubleSetting(
            "Line Width", "Width of the trajectory line", 1.5, 0.5, 4.0));

    public FishHook() {
        super("FishHook", "Shows fishing hook location and trajectory", Category.RENDER);
    }

    @EventHandler
    public void onRender3D(EventRender3D event) {
        if (mc.player == null || mc.world == null) return;

        FishingBobberEntity bobber = mc.player.fishHook;
        if (bobber == null) return;

        MatrixStack matrices = event.getMatrixStack();
        float tickDelta = event.getTickDelta();

        boolean inWater = bobber.isInWater() || bobber.isInsideWaterOrBubbleColumn();
        boolean biting  = showBiteAlert.isEnabled() && inWater;

        float r, g, b;
        if (biting) {
            r = biteColor.getRedF(); g = biteColor.getGreenF(); b = biteColor.getBlueF();
        } else {
            r = normalColor.getRedF(); g = normalColor.getGreenF(); b = normalColor.getBlueF();
        }

        // Interpolated bobber position
        double bx = bobber.prevX + (bobber.getX() - bobber.prevX) * tickDelta;
        double by = bobber.prevY + (bobber.getY() - bobber.prevY) * tickDelta;
        double bz = bobber.prevZ + (bobber.getZ() - bobber.prevZ) * tickDelta;

        if (showBox.isEnabled()) {
            Box box = new Box(bx - 0.2, by - 0.2, bz - 0.2, bx + 0.2, by + 0.2, bz + 0.2);
            RenderUtil.drawFilledBox(matrices, box, r, g, b, 0.35f);
            RenderUtil.drawESPBox(matrices, box, r, g, b, 1.0f, (float) lineWidth.get());
        }

        if (showLine.isEnabled()) {
            Vec3d from = mc.player.getEyePos();
            Vec3d to   = new Vec3d(bx, by, bz);
            RenderUtil.drawLine3D(matrices, from, to, r, g, b, 0.8f, (float) lineWidth.get());
        }
    }
}
