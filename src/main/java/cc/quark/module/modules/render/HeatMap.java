package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender3D;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.util.RenderUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class HeatMap extends Module {

    private final Map<BlockPos, Float> heatData = new HashMap<>();
    private static final float DECAY = 0.002f;
    private static final float MAX_HEAT = 100f;

    public HeatMap() {
        super("HeatMap", "Colors terrain based on recent player traffic; heat decays over time", Category.RENDER);
    }

    @Override
    public void onEnable() {
        heatData.clear();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        BlockPos pos = mc.player.getBlockPos();
        heatData.merge(pos, 5f, (a, b) -> Math.min(a + b, MAX_HEAT));
        Iterator<Map.Entry<BlockPos, Float>> it = heatData.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<BlockPos, Float> entry = it.next();
            float newVal = entry.getValue() - DECAY * 20;
            if (newVal <= 0) { it.remove(); } else { entry.setValue(newVal); }
        }
    }

    @EventHandler
    public void onRender3D(EventRender3D event) {
        if (mc.world == null || mc.player == null) return;
        MatrixStack m = event.getMatrixStack();

        for (Map.Entry<BlockPos, Float> entry : heatData.entrySet()) {
            float heat = entry.getValue() / MAX_HEAT;
            float r = heat;
            float g = 1f - heat;
            float b = 0f;
            BlockPos pos = entry.getKey();
            Box box = new Box(pos.getX(), pos.getY(), pos.getZ(),
                    pos.getX() + 1, pos.getY() + 0.05, pos.getZ() + 1);
            RenderUtil.drawFilledBox(m, box, r, g, b, heat * 0.5f);
        }
    }
}
