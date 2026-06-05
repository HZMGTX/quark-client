package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPreMotion;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class BlockInSilentAim extends Module {

    private final DoubleSetting range       = register(new DoubleSetting("Range",       "Attack range",                    3.5, 1.0, 6.0));
    private final IntSetting    speed       = register(new IntSetting   ("Speed",       "Ms between attacks",              220, 50, 1500));
    private final BoolSetting   autoBlock   = register(new BoolSetting  ("Auto Block",  "Place blocks under you while attacking", true));
    private final BoolSetting   onlyPlayers = register(new BoolSetting  ("Only Players","Only target players",             true));
    private final DoubleSetting smooth      = register(new DoubleSetting("Smooth",      "Rotation smoothing",              0.85, 0.1, 1.0));

    private float serverYaw, serverPitch;
    private long lastAttack = 0;
    private long lastPlace  = 0;

    public BlockInSilentAim() {
        super("BlockInSilentAim", "Silently aims and attacks while optionally placing blocks under feet", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        if (mc.player != null) { serverYaw = mc.player.getYaw(); serverPitch = mc.player.getPitch(); }
    }

    @EventHandler
    public void onPreMotion(EventPreMotion event) {
        event.setYaw(serverYaw);
        event.setPitch(serverPitch);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        LivingEntity target = findTarget();
        if (target != null) {
            Vec3d delta = target.getPos().add(0, target.getHeight() / 2.0, 0).subtract(mc.player.getEyePos());
            float yaw   = (float)(Math.toDegrees(Math.atan2(delta.z, delta.x)) - 90.0);
            float pitch = (float)(-Math.toDegrees(Math.atan2(delta.y, Math.sqrt(delta.x * delta.x + delta.z * delta.z))));

            float s = (float) smooth.get();
            serverYaw   = lerpAngle(serverYaw, yaw, s);
            serverPitch = MathHelper.lerp(s, serverPitch, MathHelper.clamp(pitch, -90, 90));

            long now = System.currentTimeMillis();
            if (now - lastAttack >= speed.get() && mc.player.distanceTo(target) <= range.get()) {
                mc.interactionManager.attackEntity(mc.player, target);
                mc.player.swingHand(Hand.MAIN_HAND);
                lastAttack = now;
            }
        }

        if (autoBlock.isEnabled() && System.currentTimeMillis() - lastPlace > 250) {
            tryPlaceBelow();
        }
    }

    private void tryPlaceBelow() {
        BlockPos below = mc.player.getBlockPos().down();
        if (!mc.world.getBlockState(below).isAir()) return;

        int blockSlot = findBlockSlot();
        if (blockSlot == -1) return;

        int prev = mc.player.getInventory().selectedSlot;
        mc.player.getInventory().selectedSlot = blockSlot;

        BlockHitResult hit = new BlockHitResult(
                Vec3d.ofCenter(below).add(0, 0.5, 0),
                Direction.UP,
                below,
                false);
        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
        mc.player.swingHand(Hand.MAIN_HAND);

        mc.player.getInventory().selectedSlot = prev;
        lastPlace = System.currentTimeMillis();
    }

    private int findBlockSlot() {
        for (int i = 0; i < 9; i++) {
            var stack = mc.player.getInventory().getStack(i);
            if (!stack.isEmpty() && stack.getItem() instanceof BlockItem) return i;
        }
        return -1;
    }

    private LivingEntity findTarget() {
        LivingEntity nearest = null;
        double best = range.get();
        for (Entity e : mc.world.getEntities()) {
            if (!(e instanceof LivingEntity le)) continue;
            if (le == mc.player) continue;
            if (le.isRemoved() || le.getHealth() <= 0) continue;
            if (onlyPlayers.isEnabled() && !(le instanceof PlayerEntity)) continue;
            double d = mc.player.distanceTo(le);
            if (d < best) { best = d; nearest = le; }
        }
        return nearest;
    }

    private float lerpAngle(float from, float to, float t) {
        float diff = MathHelper.wrapDegrees(to - from);
        return from + diff * t;
    }
}
