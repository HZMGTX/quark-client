package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import net.minecraft.item.BlockItem;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class TowerBuilder extends Module {

    private final IntSetting height = register(new IntSetting(
            "Height", "Tower height in blocks", 10, 1, 64));

    private final IntSetting delayMs = register(new IntSetting(
            "Delay (ms)", "Milliseconds between block placements", 100, 50, 500));

    private int built = 0;
    private long lastPlace = 0;

    public TowerBuilder() {
        super("TowerBuilder", "Builds tower of blocks under player", Category.WORLD);
    }

    @Override
    public void onEnable() { built = 0; }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (built >= height.get()) { built = 0; return; }
        if (System.currentTimeMillis() - lastPlace < delayMs.get()) return;

        int slot = -1;
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() instanceof BlockItem) {
                slot = i; break;
            }
        }
        if (slot == -1) return;

        BlockPos below = mc.player.getBlockPos().down();
        if (mc.world.getBlockState(below).isAir()) {
            int prev = mc.player.getInventory().selectedSlot;
            mc.player.getInventory().selectedSlot = slot;
            mc.player.setVelocity(mc.player.getVelocity().x, 0.42, mc.player.getVelocity().z);
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND,
                    new BlockHitResult(Vec3d.ofCenter(below), Direction.UP, below, false));
            mc.player.getInventory().selectedSlot = prev;
            built++;
            lastPlace = System.currentTimeMillis();
        }
    }
}
