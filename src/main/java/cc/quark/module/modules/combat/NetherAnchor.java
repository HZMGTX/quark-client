package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class NetherAnchor extends Module {

    private final IntSetting delay = register(new IntSetting("Delay", "Milliseconds between anchor placements", 600, 200, 2000));

    private final TimerUtil timer = new TimerUtil();

    public NetherAnchor() {
        super("NetherAnchor", "Places and detonates respawn anchors at enemy positions in the Nether", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (mc.world.getRegistryKey() != World.NETHER) return;
        if (!timer.hasReached(delay.get())) return;

        PlayerEntity target = findNearestEnemy(8);
        if (target == null) return;

        int anchorSlot = findItem(Items.RESPAWN_ANCHOR);
        if (anchorSlot == -1) return;

        BlockPos placePos = target.getBlockPos();
        if (!mc.world.getBlockState(placePos).isAir()) {
            placePos = placePos.up();
        }

        int prevSlot = mc.player.getInventory().selectedSlot;
        mc.player.getInventory().selectedSlot = anchorSlot;
        mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(anchorSlot));

        BlockHitResult hit = new BlockHitResult(Vec3d.ofCenter(placePos), Direction.UP, placePos.down(), false);
        mc.getNetworkHandler().sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, hit, 0));
        mc.player.swingHand(Hand.MAIN_HAND);

        mc.player.getInventory().selectedSlot = prevSlot;
        mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(prevSlot));
        timer.reset();
    }

    private PlayerEntity findNearestEnemy(int maxRange) {
        PlayerEntity nearest = null;
        double best = maxRange;
        for (Entity e : mc.world.getEntities()) {
            if (!(e instanceof PlayerEntity p)) continue;
            if (p == mc.player || p.isDead() || p.getHealth() <= 0) continue;
            double d = mc.player.distanceTo(p);
            if (d < best) { best = d; nearest = p; }
        }
        return nearest;
    }

    private int findItem(net.minecraft.item.Item item) {
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).isOf(item)) return i;
        }
        return -1;
    }
}
