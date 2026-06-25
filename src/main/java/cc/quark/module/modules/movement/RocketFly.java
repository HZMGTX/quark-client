package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

/**
 * RocketFly - uses firework rockets for sustained elytra propulsion, giving a
 * significant speed and altitude boost while gliding.
 *
 * <p>Features:
 * <ul>
 *   <li>Auto-launch elytra if falling and not yet gliding.</li>
 *   <li>Fires rockets from the hotbar at a configurable interval to maintain speed.</li>
 *   <li>Optional directional velocity override so the player can steer freely
 *       rather than being locked to the rocket's arc.</li>
 *   <li>Configurable minimum speed threshold before a rocket fires, to avoid
 *       wasting rockets when already moving fast.</li>
 * </ul>
 */
public class RocketFly extends Module {

    private final IntSetting interval = register(new IntSetting(
            "Interval", "Ticks between rocket fires", 20, 5, 100));

    private final DoubleSetting minSpeed = register(new DoubleSetting(
            "Min Speed", "Only fire a rocket when speed is below this value", 1.8, 0.1, 6.0));

    private final DoubleSetting thrustPower = register(new DoubleSetting(
            "Thrust Power", "Extra velocity added in the look direction each rocket fire", 0.5, 0.0, 3.0));

    private final BoolSetting autoLaunch = register(new BoolSetting(
            "Auto Launch", "Automatically start elytra glide when falling", true));

    private final BoolSetting requireKey = register(new BoolSetting(
            "Require Jump Key", "Only fire rockets while the jump key is held", false));

    private final IntSetting slot = register(new IntSetting(
            "Slot", "Hotbar slot for fireworks (-1 = auto-find)", -1, -1, 8));

    private int cooldown = 0;

    public RocketFly() {
        super("RocketFly", "Firework-rocket propulsion boost for elytra flight", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        cooldown = 0;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.interactionManager == null) return;

        // Auto-launch: start gliding when falling with an elytra equipped
        if (autoLaunch.isEnabled()) {
            boolean hasElytra = mc.player
                    .getEquippedStack(EquipmentSlot.CHEST)
                    .getItem() == Items.ELYTRA;
            if (hasElytra
                    && !mc.player.isFallFlying()
                    && !mc.player.isOnGround()
                    && mc.player.getVelocity().y < -0.15) {
                if (mc.getNetworkHandler() != null) {
                    mc.getNetworkHandler().sendPacket(
                            new ClientCommandC2SPacket(mc.player,
                                    ClientCommandC2SPacket.Mode.START_FALL_FLYING));
                }
            }
        }

        if (!mc.player.isFallFlying()) return;
        if (requireKey.isEnabled() && !mc.options.jumpKey.isPressed()) return;

        // Tick down cooldown
        if (cooldown > 0) {
            cooldown--;
            return;
        }

        // Check current speed against threshold
        Vec3d vel = mc.player.getVelocity();
        double speed = vel.length();
        if (speed >= minSpeed.get()) return;

        // Find a firework rocket in the hotbar
        int targetSlot = slot.get();
        if (targetSlot < 0 || targetSlot > 8
                || !mc.player.getInventory().getStack(targetSlot).isOf(Items.FIREWORK_ROCKET)) {
            targetSlot = findFireworkSlot();
        }
        if (targetSlot == -1) return;

        // Swap to the rocket slot, use it, swap back
        int prevSlot = mc.player.getInventory().selectedSlot;
        mc.player.getInventory().selectedSlot = targetSlot;
        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        mc.player.getInventory().selectedSlot = prevSlot;

        // Apply a directional thrust boost in the look direction
        double thrust = thrustPower.get();
        if (thrust > 0) {
            double yawRad   = Math.toRadians(mc.player.getYaw());
            double pitchRad = Math.toRadians(mc.player.getPitch());
            double lx = -Math.sin(yawRad)  * Math.cos(pitchRad);
            double ly = -Math.sin(pitchRad);
            double lz =  Math.cos(yawRad)  * Math.cos(pitchRad);
            mc.player.setVelocity(
                    vel.x + lx * thrust,
                    vel.y + ly * thrust,
                    vel.z + lz * thrust);
        }

        cooldown = interval.get();
    }

    private int findFireworkSlot() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isOf(Items.FIREWORK_ROCKET)) return i;
        }
        return -1;
    }
}
