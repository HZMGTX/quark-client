package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.util.math.BlockPos;

public class TunnelMode extends Module {

    private final BoolSetting enabled = register(new BoolSetting(
            "Enabled", "Auto-crouch to fit through 1.5-block-tall tunnels", true));

    public TunnelMode() {
        super("TunnelMode", "Automatically crouches to fit through 1.5-block-tall tunnels", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc == null || mc.player == null || mc.world == null) return;
        if (!enabled.isEnabled()) return;

        double px = mc.player.getX();
        double py = mc.player.getY();
        double pz = mc.player.getZ();

        BlockPos above = new BlockPos(
                (int) Math.floor(px),
                (int) Math.floor(py + 1.5),
                (int) Math.floor(pz));

        boolean blockAbove = !mc.world.getBlockState(above).isAir();
        BlockPos twoAbove = new BlockPos(
                (int) Math.floor(px),
                (int) Math.floor(py + 2.0),
                (int) Math.floor(pz));
        boolean clearAbove = mc.world.getBlockState(twoAbove).isAir();

        if (blockAbove && clearAbove) {
            mc.options.sneakKey.setPressed(true);
        } else if (!blockAbove) {
            mc.options.sneakKey.setPressed(false);
        }
    }

    @Override
    public void onDisable() {
        if (mc == null) return;
        mc.options.sneakKey.setPressed(false);
    }
}
