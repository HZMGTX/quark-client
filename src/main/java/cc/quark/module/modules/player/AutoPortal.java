package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.block.Blocks;
import net.minecraft.block.NetherPortalBlock;
import net.minecraft.util.math.BlockPos;

public class AutoPortal extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Range to detect portals", 2.0, 1.0, 5.0));

    private final BoolSetting nether = register(new BoolSetting(
            "Nether", "Auto-enter nether portals", true));

    private final BoolSetting end = register(new BoolSetting(
            "End", "Auto-enter end portals", true));

    public AutoPortal() {
        super("AutoPortal", "Automatically enters portals", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        int r = (int) range.get();
        BlockPos origin = mc.player.getBlockPos();

        for (int x = -r; x <= r; x++) {
            for (int y = -r; y <= r; y++) {
                for (int z = -r; z <= r; z++) {
                    BlockPos pos = origin.add(x, y, z);
                    var block = mc.world.getBlockState(pos).getBlock();

                    if (nether.isEnabled() && block == Blocks.NETHER_PORTAL) {
                        mc.player.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
                        return;
                    }
                    if (end.isEnabled() && block == Blocks.END_PORTAL) {
                        mc.player.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
                        return;
                    }
                }
            }
        }
    }
}
