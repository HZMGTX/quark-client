package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender3D;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.ColorSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.RenderUtil;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import java.util.ArrayList;
import java.util.List;

public class AnvilESP extends Module {

    private final ColorSetting color = register(new ColorSetting("Color", "ESP color for anvils and grindstones", 0xFFFFAA00));
    private final IntSetting range   = register(new IntSetting("Range", "Search radius in blocks", 32, 16, 64));

    private final List<BlockPos> found = new ArrayList<>();
    private int scanCooldown = 0;

    public AnvilESP() {
        super("AnvilESP", "Highlights anvils and grindstones in the world", Category.RENDER);
    }

    @Override
    public void onEnable() {
        found.clear();
        scanCooldown = 0;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        if (--scanCooldown > 0) return;
        scanCooldown = 40;

        found.clear();
        BlockPos center = mc.player.getBlockPos();
        int r = range.get();
        for (BlockPos pos : BlockPos.iterate(
                center.add(-r, -r, -r), center.add(r, r, r))) {
            Block block = mc.world.getBlockState(pos).getBlock();
            if (block == Blocks.ANVIL || block == Blocks.CHIPPED_ANVIL
                    || block == Blocks.DAMAGED_ANVIL || block == Blocks.GRINDSTONE) {
                found.add(pos.toImmutable());
            }
        }
    }

    @EventHandler
    public void onRender3D(EventRender3D event) {
        if (mc.world == null || mc.player == null) return;
        float r = color.getRedF(), g = color.getGreenF(), b = color.getBlueF();
        for (BlockPos pos : found) {
            Box box = new Box(pos);
            RenderUtil.drawESPBox(event.getMatrixStack(), box, r, g, b, 0.9f, 1.5f);
            RenderUtil.drawFilledBox(event.getMatrixStack(), box, r, g, b, 0.15f);
        }
    }

    @Override
    public String getSuffix() {
        return found.size() + " found";
    }
}
