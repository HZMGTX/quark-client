package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class AutoNetherFarm extends Module {

    private final IntSetting range = register(new IntSetting("Range", "Scan radius for nether wart", 5, 1, 10));
    private final IntSetting delay = register(new IntSetting("Delay", "Delay between harvest actions (ms)", 300, 100, 2000));

    private final TimerUtil timer = new TimerUtil();

    public AutoNetherFarm() {
        super("AutoNetherFarm", "Harvests and replants fully grown nether wart automatically", Category.WORLD);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(delay.get())) return;
        timer.reset();

        int r = range.get();
        BlockPos center = mc.player.getBlockPos();

        for (BlockPos pos : BlockPos.iterate(center.add(-r, -2, -r), center.add(r, 2, r))) {
            var state = mc.world.getBlockState(pos);
            if (!state.isOf(Blocks.NETHER_WART)) continue;
            if (!state.contains(Properties.AGE_3)) continue;
            if (state.get(Properties.AGE_3) < 3) continue;

            // Break the fully grown nether wart
            mc.interactionManager.attackBlock(pos.toImmutable(), Direction.UP);
            mc.player.swingHand(Hand.MAIN_HAND);

            // Replant if we have nether wart in inventory
            BlockPos soilPos = pos.down();
            if (mc.world.getBlockState(soilPos).isOf(Blocks.SOUL_SAND)) {
                int wartSlot = findNetherWartSlot();
                if (wartSlot >= 0) {
                    int prevSlot = mc.player.getInventory().selectedSlot;
                    mc.player.getInventory().selectedSlot = wartSlot;
                    BlockHitResult hit = new BlockHitResult(Vec3d.ofCenter(soilPos).add(0, 0.5, 0), Direction.UP, soilPos.toImmutable(), false);
                    mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
                    mc.player.getInventory().selectedSlot = prevSlot;
                }
            }
            return;
        }

        // Also find empty soul sand and replant
        for (BlockPos pos : BlockPos.iterate(center.add(-r, -2, -r), center.add(r, 2, r))) {
            if (!mc.world.getBlockState(pos).isOf(Blocks.SOUL_SAND)) continue;
            BlockPos above = pos.up();
            if (!mc.world.getBlockState(above).isAir()) continue;

            int wartSlot = findNetherWartSlot();
            if (wartSlot < 0) return;

            int prevSlot = mc.player.getInventory().selectedSlot;
            mc.player.getInventory().selectedSlot = wartSlot;
            BlockHitResult hit = new BlockHitResult(Vec3d.ofCenter(pos).add(0, 0.5, 0), Direction.UP, pos.toImmutable(), false);
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
            mc.player.getInventory().selectedSlot = prevSlot;
            return;
        }
    }

    private int findNetherWartSlot() {
        if (mc.player == null) return -1;
        var inv = mc.player.getInventory();
        for (int i = 0; i < 9; i++) {
            if (inv.getStack(i).isOf(Items.NETHER_WART)) return i;
        }
        for (int i = 9; i < 36; i++) {
            if (inv.getStack(i).isOf(Items.NETHER_WART)) return i;
        }
        return -1;
    }
}
