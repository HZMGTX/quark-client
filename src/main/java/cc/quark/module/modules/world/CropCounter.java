package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.block.CropBlock;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.BlockPos;

public class CropCounter extends Module {
    private final IntSetting range = register(new IntSetting("Range", "Search range", 8, 1, 32));
    private final BoolSetting onlyMature = register(new BoolSetting("Mature Only", "Count only mature crops", true));
    private int matureCount = 0, totalCount = 0;

    public CropCounter() { super("CropCounter", "Counts nearby crops on HUD", Category.WORLD); }
    @Override public void onEnable() { mc.getEventBus().subscribe(this); }
    @Override public void onDisable() { mc.getEventBus().unsubscribe(this); }

    @EventHandler
    public void onTick(EventTick e) {
        if (mc.player == null || mc.world == null) return;
        BlockPos center = mc.player.getBlockPos();
        int r = range.get(); matureCount = 0; totalCount = 0;
        for (int x = -r; x <= r; x++) for (int z = -r; z <= r; z++) {
            BlockPos pos = center.add(x, 0, z);
            var state = mc.world.getBlockState(pos);
            if (!(state.getBlock() instanceof CropBlock crop)) continue;
            totalCount++;
            if (crop.isMature(state)) matureCount++;
        }
    }

    @EventHandler
    public void onRender2D(EventRender2D e) {
        if (mc.player == null) return;
        DrawContext ctx = e.getDrawContext();
        cc.quark.util.RenderUtil.drawCustomText(ctx, "Crops: " + matureCount + "/" + totalCount + " ready", 2, 110, 0xFF55FF55);
    }
}
