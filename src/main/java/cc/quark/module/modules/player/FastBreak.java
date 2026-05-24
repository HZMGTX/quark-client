package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

/**
 * FastBreak - speeds up block breaking by artificially increasing break progress.
 *
 * Works by sending multiple dig packets per tick and manipulating the interaction
 * manager's internal break progress counter. The mixin in MixinPlayerInteractHandler
 * provides the actual speed boost hook; this module controls the settings.
 */
public class FastBreak extends Module {

    public static FastBreak INSTANCE;

    private final IntSetting speedSetting = register(new IntSetting(
            "Speed", "Break speed multiplier (1=normal, 20=instant)", 10, 1, 20));

    private final BoolSetting onlyTools = register(new BoolSetting(
            "Only Tools", "Only speed up breaking when holding the correct tool", true));

    public FastBreak() {
        super("FastBreak", "Speeds up block breaking", Category.PLAYER);
        INSTANCE = this;
    }

    /**
     * Returns the current speed multiplier. Used by MixinPlayerInteractHandler.
     */
    public float getSpeedMultiplier() {
        return speedSetting.get();
    }

    /**
     * Returns whether to restrict speedup to correct tools only.
     */
    public boolean isOnlyTools() {
        return onlyTools.isEnabled();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        // Only act when actively mining (left mouse held)
        if (!mc.options.attackKey.isPressed()) return;

        HitResult hit = mc.crosshairTarget;
        if (hit == null || hit.getType() != HitResult.Type.BLOCK) return;

        BlockHitResult blockHit = (BlockHitResult) hit;
        BlockPos pos = blockHit.getBlockPos();
        Direction face = blockHit.getSide();
        BlockState state = mc.world.getBlockState(pos);

        if (state.isAir()) return;

        // Check tool requirement
        if (onlyTools.isEnabled()) {
            ItemStack held = mc.player.getMainHandStack();
            float baseSpeed = state.calcBlockBreakingDelta(mc.player, mc.world, pos);
            // If base speed is effectively 0 (wrong tool), skip boost
            if (baseSpeed < 0.0001f) return;
        }

        // Send repeated block break packets to accelerate progress on server
        int extra = speedSetting.get() - 1;
        for (int i = 0; i < extra; i++) {
            mc.player.networkHandler.sendPacket(
                    new net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket(
                            net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket.Action.START_DESTROY_BLOCK,
                            pos,
                            face
                    )
            );
        }
    }
}
