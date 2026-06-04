package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;

public class PortalTeleport extends Module {

    private final BoolSetting nether = register(new BoolSetting(
            "Nether", "Teleport through nether portals instantly", true));

    private final BoolSetting end = register(new BoolSetting(
            "End", "Teleport through end portals instantly", true));

    public PortalTeleport() {
        super("PortalTeleport", "Teleports through portals instantly", Category.WORLD);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        BlockPos pos = mc.player.getBlockPos();
        var block = mc.world.getBlockState(pos).getBlock();

        if (nether.isEnabled() && block == Blocks.NETHER_PORTAL) {
            // Force instant teleport by setting the portal timer to max
            mc.player.setInPortal(pos);
        }

        if (end.isEnabled() && block == Blocks.END_PORTAL) {
            mc.player.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        }
    }
}
