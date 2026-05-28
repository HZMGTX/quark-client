package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender3D;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.ColorSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.RenderUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.dimension.DimensionType;

import java.util.ArrayList;
import java.util.List;

public class VoidESP extends Module {

    private final IntSetting range = register(new IntSetting(
            "Range", "Search radius in blocks", 24, 8, 64));
    private final ColorSetting color = register(new ColorSetting(
            "Color", "Void hole color", 0xFFFF2020));
    private final IntSetting voidThreshold = register(new IntSetting(
            "Void Y", "Y level considered void (columns below this)", 5, 0, 20));

    private final List<BlockPos> voidHoles = new ArrayList<>();
    private int scanTimer = 0;

    public VoidESP() {
        super("VoidESP", "Highlights air columns that drop to the void", Category.RENDER);
    }

    @Override
    public void onEnable() {
        voidHoles.clear();
        scanTimer = 0;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        scanTimer--;
        if (scanTimer > 0) return;
        scanTimer = 20;
        scan();
    }

    private void scan() {
        voidHoles.clear();
        if (mc.world == null || mc.player == null) return;

        int r = range.get();
        int threshold = voidThreshold.get();
        BlockPos center = mc.player.getBlockPos();

        for (int x = center.getX() - r; x <= center.getX() + r; x++) {
            for (int z = center.getZ() - r; z <= center.getZ() + r; z++) {
                int scanY = threshold;
                boolean allAir = true;
                for (int y = scanY; y <= center.getY(); y++) {
                    BlockPos check = new BlockPos(x, y, z);
                    if (!mc.world.getBlockState(check).isAir()) {
                        allAir = false;
                        break;
                    }
                }
                if (allAir) {
                    voidHoles.add(new BlockPos(x, center.getY(), z));
                }
            }
        }
    }

    @EventHandler
    public void onRender3D(EventRender3D event) {
        if (mc.world == null || mc.player == null) return;
        float r = color.getRedF();
        float g = color.getGreenF();
        float b = color.getBlueF();

        long ms = System.currentTimeMillis();
        float pulse = (float)(Math.sin(ms * 0.002) * 0.3 + 0.4);

        for (BlockPos pos : voidHoles) {
            Box topSlice = new Box(pos.getX(), pos.getY(), pos.getZ(),
                    pos.getX() + 1, pos.getY() + 0.05, pos.getZ() + 1);
            RenderUtil.drawFilledBox(event.getMatrixStack(), topSlice, r, g, b, pulse);
            RenderUtil.drawESPBox(event.getMatrixStack(), topSlice.expand(0, 0.02, 0), r, g, b, 0.9f, 1.2f);
        }
    }

    @Override
    public String getSuffix() {
        return voidHoles.size() + " holes";
    }
}
