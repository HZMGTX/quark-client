package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.ModeSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class AutoNetherFarm2 extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Farming scan radius", 4.0, 1.0, 8.0));
    private final ModeSetting resource = register(new ModeSetting(
            "Resource", "Which nether resource to farm",
            "Quartz", "Quartz", "Gold", "Wart"));

    private static final Set<Block> QUARTZ_ORE = new HashSet<>(Arrays.asList(
            Blocks.NETHER_QUARTZ_ORE));
    private static final Set<Block> GOLD_ORE = new HashSet<>(Arrays.asList(
            Blocks.NETHER_GOLD_ORE));

    private final TimerUtil timer = new TimerUtil();

    public AutoNetherFarm2() {
        super("AutoNetherFarm2", "Enhanced nether resource farming for quartz, gold, and wart", Category.WORLD);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(250)) return;
        timer.reset();

        int r = (int) Math.ceil(range.get());
        double rangeSq = range.get() * range.get();
        BlockPos center = mc.player.getBlockPos();

        for (BlockPos pos : BlockPos.iterate(center.add(-r, -r, -r), center.add(r, r, r))) {
            if (pos.getSquaredDistance(mc.player.getPos()) > rangeSq) continue;
            Block block = mc.world.getBlockState(pos).getBlock();

            boolean match = switch (resource.get()) {
                case "Quartz" -> QUARTZ_ORE.contains(block);
                case "Gold"   -> GOLD_ORE.contains(block);
                case "Wart"   -> {
                    if (block != Blocks.NETHER_WART) yield false;
                    var state = mc.world.getBlockState(pos);
                    yield state.contains(Properties.AGE_3) && state.get(Properties.AGE_3) == 3;
                }
                default -> false;
            };

            if (!match) continue;

            mc.interactionManager.attackBlock(pos.toImmutable(), Direction.UP);
            mc.player.swingHand(Hand.MAIN_HAND);

            // Replant wart if needed
            if (resource.get().equals("Wart")) {
                BlockPos soil = pos.down();
                if (mc.world.getBlockState(soil).isOf(Blocks.SOUL_SAND)) {
                    int wartSlot = findItem(Items.NETHER_WART);
                    if (wartSlot >= 0) {
                        int prev = mc.player.getInventory().selectedSlot;
                        mc.player.getInventory().selectedSlot = wartSlot;
                        net.minecraft.util.hit.BlockHitResult hit = new net.minecraft.util.hit.BlockHitResult(
                                net.minecraft.util.math.Vec3d.ofCenter(soil).add(0, 0.5, 0),
                                Direction.UP, soil.toImmutable(), false);
                        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
                        mc.player.getInventory().selectedSlot = prev;
                    }
                }
            }
            return;
        }
    }

    private int findItem(net.minecraft.item.Item item) {
        if (mc.player == null) return -1;
        for (int i = 0; i < 36; i++) {
            if (mc.player.getInventory().getStack(i).isOf(item)) return i;
        }
        return -1;
    }
}
