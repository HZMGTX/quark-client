package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class AutoWither2 extends Module {
    private final BoolSetting autoMaterials = register(new BoolSetting("AutoMaterials", "Auto-collect soul sand and skulls", false));
    private final IntSetting buildDelay = register(new IntSetting("BuildDelay", "Ticks between placements", 5, 1, 20));
    private int tick = 0;
    private int step = 0;
    public AutoWither2() { super("AutoWither2", "Automatically builds the wither boss structure", Category.WORLD); }
    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || ++tick < buildDelay.getValue()) return;
        tick = 0;
        // Build wither at player position
    }
}
