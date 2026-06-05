package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender3D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.ColorSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.util.RenderUtil;
import net.minecraft.block.BedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

/**
 * BedESP - Highlights all bed blocks within render range.
 * Useful on servers like Hypixel BedWars for locating enemy beds.
 */
public class BedESP extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Block scan range in blocks", 64.0, 16.0, 128.0));

    private final ColorSetting fillColor = register(new ColorSetting(
            "Fill Color", "Bed highlight fill color", 0x55FF4444));

    private final ColorSetting outlineColor = register(new ColorSetting(
            "Outline Color", "Bed highlight outline color", 0xFFFF4444));

    private final BoolSetting showDistance = register(new BoolSetting(
            "Show Distance", "Show distance to each bed in the 3D world", false));

    private final DoubleSetting lineWidth = register(new DoubleSetting(
            "Line Width", "Outline line width", 1.5, 1.0, 3.0));

    public BedESP() {
        super("BedESP", "Highlights all beds within range for BedWars and similar gamemodes", Category.PLAYER);
    }

    @EventHandler
    public void onRender3D(EventRender3D event) {
        if (mc.world == null || mc.player == null) return;

        MatrixStack matrices = event.getMatrixStack();
        double scanRange = range.get();
        BlockPos playerPos = mc.player.getBlockPos();

        int rI = (int) Math.ceil(scanRange);
        for (int dx = -rI; dx <= rI; dx++) {
            for (int dy = -rI; dy <= rI; dy++) {
                for (int dz = -rI; dz <= rI; dz++) {
                    BlockPos pos = playerPos.add(dx, dy, dz);
                    double dist = mc.player.getPos().distanceTo(Vec3d.ofCenter(pos));
                    if (dist > scanRange) continue;

                    BlockState state = mc.world.getBlockState(pos);
                    if (!(state.getBlock() instanceof BedBlock)) continue;

                    // Only render one part of the bed (the foot or head, not both)
                    // BedBlock has a PART property; we render both parts independently
                    Box box = new Box(pos.getX(), pos.getY(), pos.getZ(),
                            pos.getX() + 1.0, pos.getY() + 0.5625, pos.getZ() + 1.0);

                    float fr = outlineColor.getRedF();
                    float fg = outlineColor.getGreenF();
                    float fb = outlineColor.getBlueF();
                    float fa = outlineColor.getAlphaF();

                    RenderUtil.drawBox(matrices, box, fr, fg, fb, fillColor.getAlphaF() / 255f, (float) lineWidth.get());
                }
            }
        }
    }
}
