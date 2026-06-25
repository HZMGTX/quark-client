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

public class TrapESP extends Module {

    private final ColorSetting color = register(new ColorSetting("Color", "ESP color for traps", 0xFFFF4444));
    private final IntSetting range   = register(new IntSetting("Range", "Search radius in blocks", 24, 8, 64));

    private final List<BlockPos> found = new ArrayList<>();
    private int scanCooldown = 0;

    public TrapESP() {
        super("TrapESP", "Highlights pressure plates, tripwires, and TNT blocks", Category.RENDER);
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
        scanCooldown = 30;

        found.clear();
        BlockPos center = mc.player.getBlockPos();
        int r = range.get();
        for (BlockPos pos : BlockPos.iterate(
                center.add(-r, -r, -r), center.add(r, r, r))) {
            Block block = mc.world.getBlockState(pos).getBlock();
            if (isTrap(block)) found.add(pos.toImmutable());
        }
    }

    private boolean isTrap(Block block) {
        return block == Blocks.STONE_PRESSURE_PLATE
                || block == Blocks.OAK_PRESSURE_PLATE
                || block == Blocks.SPRUCE_PRESSURE_PLATE
                || block == Blocks.BIRCH_PRESSURE_PLATE
                || block == Blocks.JUNGLE_PRESSURE_PLATE
                || block == Blocks.ACACIA_PRESSURE_PLATE
                || block == Blocks.DARK_OAK_PRESSURE_PLATE
                || block == Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE
                || block == Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE
                || block == Blocks.TRIPWIRE_HOOK
                || block == Blocks.TRIPWIRE
                || block == Blocks.TNT;
    }

    @EventHandler
    public void onRender3D(EventRender3D event) {
        if (mc.world == null || mc.player == null) return;
        float r = color.getRedF(), g = color.getGreenF(), b = color.getBlueF();
        for (BlockPos pos : found) {
            Box box = new Box(pos);
            RenderUtil.drawESPBox(event.getMatrixStack(), box, r, g, b, 0.9f, 1.5f);
            RenderUtil.drawFilledBox(event.getMatrixStack(), box, r, g, b, 0.20f);
        }
    }

    @Override
    public String getSuffix() {
        return found.size() + " traps";
    }
}
