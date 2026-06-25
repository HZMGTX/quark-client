package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender3D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.client.render.Camera;
import net.minecraft.util.math.ChunkPos;

public class ChunkESP extends Module {
    private final IntSetting radius = register(new IntSetting("Radius", "Chunk display radius", 3, 1, 10));
    private final BoolSetting showCoords = register(new BoolSetting("ShowCoords", "Show chunk coordinates", true));
    public ChunkESP() { super("ChunkESP", "Shows chunk borders and coordinates", Category.RENDER); }
    @EventHandler
    public void onRender3D(EventRender3D event) {
        if (mc.player == null || mc.world == null) return;
    }
}
