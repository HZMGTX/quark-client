package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender3D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.ColorSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.RenderUtil;
import net.minecraft.item.*;
import net.minecraft.util.math.Vec3d;

public class Trajectories extends Module {

    private final BoolSetting arrows  = register(new BoolSetting("Arrows",     "Predict arrow trajectory",          true));
    private final BoolSetting pearls  = register(new BoolSetting("Ender Pearl", "Predict ender pearl trajectory",   true));
    private final BoolSetting potions = register(new BoolSetting("Potions",    "Predict splash potion trajectory",  true));
    private final BoolSetting snowballs = register(new BoolSetting("Snowballs","Predict snowball/egg trajectory",  false));
    private final IntSetting  steps   = register(new IntSetting("Steps", "Simulation steps (higher = further)", 60, 20, 120));
    private final ColorSetting color  = register(new ColorSetting("Color", "Trajectory line color", 0xFF44FF44));

    public Trajectories() {
        super("Trajectories", "Renders predicted projectile trajectories", Category.RENDER);
    }

    @EventHandler
    public void onRender3D(EventRender3D event) {
        if (mc.player == null || mc.world == null) return;

        ItemStack held = mc.player.getMainHandStack();
        Item item = held.getItem();

        double gravity;
        double speed;

        if (item instanceof BowItem && arrows.isEnabled()) {
            gravity = 0.05;
            speed = 3.0;
        } else if (item instanceof CrossbowItem && arrows.isEnabled()) {
            gravity = 0.05;
            speed = 3.15;
        } else if (item instanceof EnderPearlItem && pearls.isEnabled()) {
            gravity = 0.03;
            speed = 1.5;
        } else if ((item instanceof SplashPotionItem || item instanceof LingeringPotionItem) && potions.isEnabled()) {
            gravity = 0.05;
            speed = 0.5;
        } else if ((item instanceof SnowballItem || item instanceof EggItem) && snowballs.isEnabled()) {
            gravity = 0.03;
            speed = 1.5;
        } else {
            return;
        }

        float yaw   = (float) Math.toRadians(mc.player.getYaw());
        float pitch = (float) Math.toRadians(mc.player.getPitch());

        double vx = -Math.sin(yaw) * Math.cos(pitch) * speed;
        double vy = -Math.sin(pitch) * speed;
        double vz =  Math.cos(yaw) * Math.cos(pitch) * speed;

        Vec3d pos = mc.player.getEyePos();

        int col = color.get();
        float r = ((col >> 16) & 0xFF) / 255f;
        float g = ((col >> 8)  & 0xFF) / 255f;
        float b = (col         & 0xFF) / 255f;
        float a = ((col >> 24) & 0xFF) / 255f;

        for (int i = 0; i < steps.get() - 1; i++) {
            Vec3d nextPos = pos.add(vx, vy, vz);
            vx *= 0.99;
            vy -= gravity;
            vz *= 0.99;

            // Stop on block hit
            if (mc.world.getBlockState(new net.minecraft.util.math.BlockPos((int)nextPos.x, (int)nextPos.y, (int)nextPos.z)).isSolid()) break;

            RenderUtil.drawLine3D(event.getMatrixStack(), pos, nextPos, r, g, b, a, 1.5f);
            pos = nextPos;
        }
    }
}
