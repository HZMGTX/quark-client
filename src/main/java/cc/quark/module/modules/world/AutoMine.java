package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.setting.ModeSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class AutoMine extends Module {

    private final ModeSetting mode = register(new ModeSetting(
            "Mode", "Mining behavior",
            "Continuous",
            "Continuous", "Single", "Vein"));

    private final BoolSetting crosshair = register(new BoolSetting(
            "Target Crosshair", "Mine whatever block the crosshair points to", true));

    private final IntSetting delayMs = register(new IntSetting(
            "Delay", "Milliseconds between block breaks", 50, 0, 1000));

    private final TimerUtil timer = new TimerUtil();
    private BlockPos lastTarget = null;
    private boolean mined = false;

    public AutoMine() {
        super("AutoMine", "Auto-mines the targeted block continuously or in single/vein modes", Category.WORLD);
    }

    @Override
    public void onEnable() {
        lastTarget = null;
        mined = false;
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(delayMs.get())) return;

        if (!crosshair.isEnabled()) return;

        HitResult hr = mc.crosshairTarget;
        if (hr == null || hr.getType() != HitResult.Type.BLOCK) {
            lastTarget = null;
            mined = false;
            return;
        }

        BlockHitResult bhr = (BlockHitResult) hr;
        BlockPos pos = bhr.getBlockPos();
        Direction face = bhr.getSide();

        if (mc.world.getBlockState(pos).isAir()) {
            lastTarget = null;
            mined = false;
            return;
        }

        String m = mode.get();

        if (m.equals("Single")) {
            if (mined && pos.equals(lastTarget)) return;
        }

        if (!pos.equals(lastTarget)) {
            lastTarget = pos;
            mined = false;
            mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(
                    PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, pos, face));
        }

        mc.interactionManager.attackBlock(pos, face);
        mc.player.swingHand(Hand.MAIN_HAND);

        if (mc.world.getBlockState(pos).isAir()) {
            mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(
                    PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, pos, face));
            mined = true;

            if (m.equals("Vein")) {
                lastTarget = null;
            }
            timer.reset();
        }
    }
}
