package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.block.entity.BarrelBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

/**
 * ChestAura - opens nearby chests, barrels, and shulker boxes automatically.
 * Optionally closes the container after a configurable delay.
 */
public class ChestAura extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Range to search containers", 4.0, 2.0, 6.0));

    private final BoolSetting autoClose = register(new BoolSetting(
            "Auto Close", "Close chest after opening", true));

    private final IntSetting closeDelay = register(new IntSetting(
            "Close Delay", "Ticks to wait before closing", 40, 20, 100));

    private final TimerUtil openTimer = new TimerUtil();

    private BlockPos targetChest    = null;
    private boolean  containerOpen  = false;
    private int      closeCountdown = -1;

    public ChestAura() {
        super("ChestAura", "Automatically opens nearby chests/barrels/shulkers", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        targetChest    = null;
        containerOpen  = false;
        closeCountdown = -1;
    }

    @Override
    public void onDisable() {
        if (mc.currentScreen instanceof HandledScreen<?> && mc.player != null) {
            mc.player.closeHandledScreen();
        }
        targetChest    = null;
        containerOpen  = false;
        closeCountdown = -1;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        // Handle auto-close countdown
        if (containerOpen && autoClose.isEnabled()) {
            if (closeCountdown < 0) {
                closeCountdown = closeDelay.get();
            } else if (closeCountdown == 0) {
                if (mc.currentScreen instanceof HandledScreen<?>) {
                    mc.player.closeHandledScreen();
                }
                containerOpen  = false;
                targetChest    = null;
                closeCountdown = -1;
                return;
            } else {
                closeCountdown--;
                return;
            }
        }

        // Track whether a container is currently open
        if (mc.currentScreen instanceof HandledScreen<?>) {
            containerOpen = true;
            return;
        }

        // Container was closed or never opened — reset and search
        containerOpen  = false;
        closeCountdown = -1;

        BlockPos playerPos = mc.player.getBlockPos();
        double r = range.get();
        BlockPos nearest     = null;
        double  nearestDist  = Double.MAX_VALUE;

        for (BlockPos pos : BlockPos.iterate(
                (int)(playerPos.getX() - r), (int)(playerPos.getY() - r), (int)(playerPos.getZ() - r),
                (int)(playerPos.getX() + r), (int)(playerPos.getY() + r), (int)(playerPos.getZ() + r))) {

            double dist = mc.player.getPos().distanceTo(Vec3d.ofCenter(pos));
            if (dist > r) continue;

            BlockEntity be = mc.world.getBlockEntity(pos);
            if (be instanceof ChestBlockEntity
                    || be instanceof BarrelBlockEntity
                    || be instanceof ShulkerBoxBlockEntity) {
                if (dist < nearestDist) {
                    nearestDist = dist;
                    nearest     = pos.toImmutable();
                }
            }
        }

        if (nearest != null && !nearest.equals(targetChest)) {
            targetChest = nearest;
            mc.interactionManager.interactBlock(
                    mc.player,
                    Hand.MAIN_HAND,
                    new BlockHitResult(Vec3d.ofCenter(nearest), Direction.UP, nearest, false)
            );
            openTimer.reset();
        }
    }
}
