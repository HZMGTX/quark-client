package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.HitResult;

public class ReachDisplay extends Module {

    private final IntSetting  posX       = register(new IntSetting("X", "HUD X position", 4, 0, 3000));
    private final IntSetting  posY       = register(new IntSetting("Y", "HUD Y position", 20, 0, 3000));
    private final BoolSetting showTarget = register(new BoolSetting("Show Target", "Show what the player is targeting", true));
    private final BoolSetting colorCode  = register(new BoolSetting("Color Code",  "Color based on reach distance",     true));

    /** Default survival reach distance. */
    private static final double VANILLA_REACH = 4.5;
    private static final double CREATIVE_REACH = 5.0;

    public ReachDisplay() {
        super("ReachDisplay", "Shows current reach distance and target info on HUD", Category.RENDER);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null || mc.crosshairTarget == null) return;

        DrawContext ctx = event.getDrawContext();
        int x = posX.get(), y = posY.get();
        int lh = mc.textRenderer.fontHeight + 2;

        HitResult hit = mc.crosshairTarget;
        double dist = hit.getPos().distanceTo(mc.player.getEyePos());
        double maxReach = mc.player.getAbilities().creativeMode ? CREATIVE_REACH : VANILLA_REACH;

        int color;
        if (!colorCode.isEnabled()) {
            color = 0xFFFFFFFF;
        } else if (dist <= maxReach - 0.5) {
            color = 0xFF55FF55;
        } else if (dist <= maxReach) {
            color = 0xFFFFFF55;
        } else {
            color = 0xFFFF5555;
        }

        ctx.drawTextWithShadow(mc.textRenderer,
                String.format("Reach: §f%.2f§r / §f%.1f", dist, maxReach),
                x, y, color);

        if (showTarget.isEnabled()) {
            String targetStr = switch (hit.getType()) {
                case BLOCK  -> "Block: " + mc.world.getBlockState(
                        net.minecraft.util.hit.BlockHitResult.class.cast(hit).getBlockPos()
                ).getBlock().getName().getString();
                case ENTITY -> "Entity: " + ((net.minecraft.util.hit.EntityHitResult) hit)
                        .getEntity().getType().getName().getString();
                default     -> "Target: None";
            };
            ctx.drawTextWithShadow(mc.textRenderer, "§7" + targetStr, x, y + lh, 0xFFAAAAAA);
        }
    }
}
