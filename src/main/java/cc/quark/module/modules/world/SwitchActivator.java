package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.block.ButtonBlock;
import net.minecraft.block.LeverBlock;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class SwitchActivator extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Radius to search for levers and buttons", 3.0, 1.0, 6.0));
    private final BoolSetting buttons = register(new BoolSetting(
            "Buttons", "Activate nearby buttons", true));
    private final BoolSetting levers = register(new BoolSetting(
            "Levers", "Activate nearby levers", true));

    private final TimerUtil timer = new TimerUtil();

    public SwitchActivator() {
        super("SwitchActivator", "Auto-activates levers and pressure plates", Category.WORLD);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(250)) return;

        int r = (int) Math.ceil(range.get());
        double rangeSq = range.get() * range.get();
        BlockPos center = mc.player.getBlockPos();

        for (BlockPos pos : BlockPos.iterate(center.add(-r, -r, -r), center.add(r, r, r))) {
            if (pos.getSquaredDistance(mc.player.getPos()) > rangeSq) continue;
            var state = mc.world.getBlockState(pos);

            boolean isLever  = levers.isEnabled()  && state.getBlock() instanceof LeverBlock;
            boolean isButton = buttons.isEnabled() && state.getBlock() instanceof ButtonBlock;
            if (!isLever && !isButton) continue;

            BlockHitResult hit = new BlockHitResult(Vec3d.ofCenter(pos), Direction.UP, pos, false);
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
            mc.player.swingHand(Hand.MAIN_HAND);
            timer.reset();
            return;
        }
    }
}
