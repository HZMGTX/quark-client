package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.util.math.BlockPos;

public class StairSit extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Range to search for sittable blocks", 2.0, 1.0, 5.0));
    private final BoolSetting onIdle = register(new BoolSetting(
            "OnIdle", "Only sit when not moving", true));

    private final TimerUtil idleTimer = new TimerUtil();

    public StairSit() {
        super("StairSit", "Automatically sits on stairs/slabs when idle", Category.PLAYER);
    }

    @Override
    public void onDisable() {
        if (mc.player != null) {
            mc.player.setSneaking(false);
            mc.options.sneakKey.setPressed(false);
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        if (onIdle.isEnabled()) {
            boolean moving = mc.player.getVelocity().horizontalLength() > 0.01;
            if (moving) {
                idleTimer.reset();
                mc.player.setSneaking(false);
                mc.options.sneakKey.setPressed(false);
                return;
            }
            if (!idleTimer.hasReached(1000)) return;
        }

        BlockPos below = mc.player.getBlockPos().down();
        var block = mc.world.getBlockState(below).getBlock();

        boolean sittable = (block instanceof StairsBlock || block instanceof SlabBlock);
        mc.player.setSneaking(sittable);
        mc.options.sneakKey.setPressed(sittable);
    }
}
