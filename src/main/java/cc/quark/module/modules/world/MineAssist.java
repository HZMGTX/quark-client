package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.block.Block;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class MineAssist extends Module {

    private final IntSetting radius = register(new IntSetting("Radius", "Radius to check for same-type blocks", 1, 0, 2));
    private final BoolSetting sameType = register(new BoolSetting("Same Type", "Only break blocks of the same type", true));

    public MineAssist() {
        super("MineAssist", "Also breaks nearby blocks of same type while mining", Category.WORLD);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!mc.options.attackKey.isPressed()) return;

        HitResult hit = mc.crosshairTarget;
        if (hit == null || hit.getType() != HitResult.Type.BLOCK) return;

        BlockPos origin = ((BlockHitResult) hit).getBlockPos();
        Block target = mc.world.getBlockState(origin).getBlock();
        if (mc.world.getBlockState(origin).getHardness(mc.world, origin) < 0) return;

        int r = radius.get();
        for (int dx = -r; dx <= r; dx++) {
            for (int dy = -r; dy <= r; dy++) {
                for (int dz = -r; dz <= r; dz++) {
                    if (dx == 0 && dy == 0 && dz == 0) continue;
                    BlockPos pos = origin.add(dx, dy, dz);
                    Block b = mc.world.getBlockState(pos).getBlock();
                    if (sameType.isEnabled() && b != target) continue;
                    if (mc.world.getBlockState(pos).isAir()) continue;
                    mc.interactionManager.attackBlock(pos, Direction.UP);
                }
            }
        }
    }
}
