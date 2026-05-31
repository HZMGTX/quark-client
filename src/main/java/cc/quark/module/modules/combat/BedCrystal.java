package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class BedCrystal extends Module {

    private final IntSetting switchDelay = register(new IntSetting("SwitchDelay", "Milliseconds between bed and crystal switch", 250, 50, 1000));

    private final TimerUtil timer = new TimerUtil();
    private boolean placeBed = true;

    public BedCrystal() {
        super("BedCrystal", "Alternates placing bed and crystal for combo damage in Nether/End", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        timer.reset();
        placeBed = true;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        World.RegistryKey key = mc.world.getRegistryKey();
        if (key != World.NETHER && key != World.END) return;
        if (!timer.hasReached(switchDelay.get())) return;

        PlayerEntity target = findNearestEnemy(6);
        if (target == null) return;

        if (placeBed) {
            int bedSlot = findBedSlot();
            if (bedSlot == -1) { placeBed = false; return; }
            BlockPos pos = target.getBlockPos();
            placeBed(bedSlot, pos);
        } else {
            int crystalSlot = findCrystalSlot();
            if (crystalSlot == -1) { placeBed = true; return; }
            BlockPos pos = target.getBlockPos().up();
            placeCrystal(crystalSlot, pos);
            breakNearbyCrystal(target);
        }

        placeBed = !placeBed;
        timer.reset();
    }

    private void placeBed(int slot, BlockPos pos) {
        int prev = mc.player.getInventory().selectedSlot;
        mc.player.getInventory().selectedSlot = slot;
        mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(slot));
        BlockHitResult hit = new BlockHitResult(Vec3d.ofCenter(pos), Direction.UP, pos.down(), false);
        mc.getNetworkHandler().sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, hit, 0));
        mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
        mc.player.getInventory().selectedSlot = prev;
        mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(prev));
    }

    private void placeCrystal(int slot, BlockPos pos) {
        int prev = mc.player.getInventory().selectedSlot;
        mc.player.getInventory().selectedSlot = slot;
        mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(slot));
        BlockHitResult hit = new BlockHitResult(Vec3d.ofCenter(pos), Direction.UP, pos.down(), false);
        mc.getNetworkHandler().sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, hit, 0));
        mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
        mc.player.getInventory().selectedSlot = prev;
        mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(prev));
    }

    private void breakNearbyCrystal(PlayerEntity target) {
        for (Entity e : mc.world.getEntities()) {
            if (!(e instanceof EndCrystalEntity crystal)) continue;
            if (crystal.distanceTo(target) > 4) continue;
            mc.getNetworkHandler().sendPacket(PlayerInteractEntityC2SPacket.attack(crystal, false));
            mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
            break;
        }
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

    private int findBedSlot() {
        net.minecraft.item.Item[] beds = {
            Items.WHITE_BED, Items.RED_BED, Items.BLUE_BED, Items.GREEN_BED,
            Items.YELLOW_BED, Items.ORANGE_BED, Items.BLACK_BED, Items.BROWN_BED
        };
        for (int i = 0; i < 9; i++) {
            net.minecraft.item.Item item = mc.player.getInventory().getStack(i).getItem();
            for (net.minecraft.item.Item bed : beds) {
                if (item == bed) return i;
            }
        }
        return -1;
    }

    private int findCrystalSlot() {
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).isOf(Items.END_CRYSTAL)) return i;
        }
        return -1;
    }
}
