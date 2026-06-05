package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.ChatUtil;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import java.util.ArrayList;
import java.util.List;

public class StructureSave extends Module {
    private final IntSetting radius = register(new IntSetting("Radius", "Capture radius", 8, 1, 32));
    private final BoolSetting includeAir = register(new BoolSetting("Include Air", "Include air blocks", false));
    private boolean capturing = false;

    public StructureSave() { super("StructureSave", "Saves nearby block structure to memory", Category.WORLD); }
    @Override public void onEnable() { mc.getEventBus().subscribe(this); capture(); }
    @Override public void onDisable() { mc.getEventBus().unsubscribe(this); }

    private void capture() {
        if (mc.player == null || mc.world == null) return;
        BlockPos center = mc.player.getBlockPos();
        int r = radius.get();
        int count = 0;
        for (int x = -r; x <= r; x++) {
            for (int y = -r; y <= r; y++) {
                for (int z = -r; z <= r; z++) {
                    BlockPos pos = center.add(x, y, z);
                    BlockState state = mc.world.getBlockState(pos);
                    if (!includeAir.isEnabled() && state.isAir()) continue;
                    count++;
                }
            }
        }
        ChatUtil.info("Captured " + count + " blocks in radius " + r);
    }

    @EventHandler
    public void onTick(EventTick e) { }
}
