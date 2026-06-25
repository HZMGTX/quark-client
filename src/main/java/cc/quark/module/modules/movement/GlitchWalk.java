package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

/**
 * GlitchWalk - uses vanilla step / micro-clip logic to pass through thin (1-block)
 * walls.  Alternates between a slightly elevated position and the floor each tick
 * while moving into a wall, exploiting the fact that the server accepts the snapped
 * position before the collision is resolved client-side.
 *
 * This is a client-side positional glitch; effect depends on server tolerance.
 */
public class GlitchWalk extends Module {

    private final DoubleSetting clipHeight = register(new DoubleSetting(
            "Clip Height", "Y-offset applied on the clip tick (0.05–0.25 works on most servers)", 0.1, 0.01, 0.5));

    private final IntSetting clipInterval = register(new IntSetting(
            "Clip Interval", "Ticks between clip pulses (lower = more aggressive)", 2, 1, 10));

    private final BoolSetting onlyWhenBlocked = register(new BoolSetting(
            "Only When Blocked", "Only clip when a wall is detected ahead", true));

    private int tickCounter = 0;
    private boolean clipPhase = false;

    public GlitchWalk() {
        super("GlitchWalk", "Clips through thin walls using movement glitches", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        tickCounter = 0;
        clipPhase = false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        boolean moving = mc.player.input.movementForward != 0
                || mc.player.input.movementSideways != 0;
        if (!moving) return;

        if (onlyWhenBlocked.isEnabled() && !isWallAhead()) return;

        tickCounter++;
        if (tickCounter < clipInterval.get()) return;
        tickCounter = 0;

        clipPhase = !clipPhase;
        if (clipPhase) {
            // Slight upward nudge to clip over the wall threshold
            Vec3d vel = mc.player.getVelocity();
            mc.player.setVelocity(vel.x, clipHeight.get(), vel.z);
            mc.player.fallDistance = 0;
        }
    }

    /** Returns true if there is a solid block directly in front of the player. */
    private boolean isWallAhead() {
        Vec3d vel = mc.player.getVelocity();
        if (Math.abs(vel.x) < 0.001 && Math.abs(vel.z) < 0.001) return false;

        Vec3d pos = mc.player.getPos();
        double nx = pos.x + vel.x * 3;
        double nz = pos.z + vel.z * 3;

        BlockPos nextPos = new BlockPos((int) Math.floor(nx), (int) Math.floor(pos.y), (int) Math.floor(nz));
        boolean feetBlocked = mc.world.getBlockState(nextPos).isSolid();
        boolean headBlocked = mc.world.getBlockState(nextPos.up()).isSolid();

        return feetBlocked || headBlocked;
    }
}
