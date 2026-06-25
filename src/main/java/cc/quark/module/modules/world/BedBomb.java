package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.item.BedItem;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class BedBomb extends Module {

    private final IntSetting delay = register(new IntSetting(
            "Delay", "Milliseconds between bed bomb attempts", 500, 100, 5000));

    private final TimerUtil timer = new TimerUtil();

    public BedBomb() {
        super("BedBomb", "Places and activates beds in the Nether/End to cause explosions", Category.WORLD);
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

        RegistryKey<World> dim = mc.world.getRegistryKey();
        boolean isDangerous = dim == World.NETHER || dim == World.END;
        if (!isDangerous) return;

        int bedSlot = -1;
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() instanceof BedItem) {
                bedSlot = i;
                break;
            }
        }
        if (bedSlot == -1) return;

        BlockPos placePos = mc.player.getBlockPos().offset(mc.player.getHorizontalFacing());
        if (!mc.world.getBlockState(placePos).isAir()) return;
        BlockPos support = placePos.down();
        if (!mc.world.getBlockState(support).isSolidBlock(mc.world, support)) return;

        int saved = mc.player.getInventory().selectedSlot;
        mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(bedSlot));
        mc.player.getInventory().selectedSlot = bedSlot;
        BlockHitResult hit = new BlockHitResult(Vec3d.ofCenter(support).add(0, 0.5, 0), Direction.UP, support, false);
        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);

        BlockHitResult useHit = new BlockHitResult(Vec3d.ofCenter(placePos), Direction.UP, placePos, false);
        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, useHit);
        mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(saved));
        mc.player.getInventory().selectedSlot = saved;
    }
}
