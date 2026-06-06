package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.setting.ModeSetting;
import net.minecraft.util.math.BlockPos;

public class AutoTerraformer extends Module {

    private final IntSetting range = register(new IntSetting("Range", "Terraform range", 5, 3, 15));
    private final ModeSetting mode = register(new ModeSetting("Mode", "Terraform mode", "Flatten", "Flatten", "Clear", "Fill"));
    private final IntSetting targetY = register(new IntSetting("TargetY", "Target Y level for flatten", 64, -64, 320));

    private int tickDelay = 0;

    public AutoTerraformer() {
        super("AutoTerraformer", "Automatically levels terrain in an area", Category.WORLD);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (++tickDelay < 2) return;
        tickDelay = 0;

        int px = (int) mc.player.getX(), pz = (int) mc.player.getZ();
        int r = range.getValue();

        for (int x = px - r; x <= px + r; x++) {
            for (int z = pz - r; z <= pz + r; z++) {
                if ("Flatten".equals(mode.getValue()) || "Clear".equals(mode.getValue())) {
                    for (int y = targetY.getValue() + 1; y <= targetY.getValue() + 10; y++) {
                        BlockPos pos = new BlockPos(x, y, z);
                        if (!mc.world.getBlockState(pos).isAir()) {
                            mc.interactionManager.attackBlock(pos, net.minecraft.util.math.Direction.UP);
                            return;
                        }
                    }
                }
            }
        }
    }
}
