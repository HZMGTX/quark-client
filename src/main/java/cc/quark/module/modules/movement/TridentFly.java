package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.Vec3d;

public class TridentFly extends Module {

    private final BoolSetting autoEquip = register(new BoolSetting(
            "AutoEquip", "Automatically swap trident to main hand", true));

    public TridentFly() {
        super("TridentFly", "Uses riptide trident to fly in rain or water", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        ClientPlayerEntity p = mc.player;
        boolean inWater = p.isTouchingWater();
        boolean inRain = mc.world.hasRain(p.getBlockPos());

        if (!inWater && !inRain) return;

        ItemStack mainHand = p.getMainHandStack();
        boolean hasTrident = mainHand.getItem() == Items.TRIDENT;

        if (!hasTrident && autoEquip.isEnabled()) {
            int slot = findTrident();
            if (slot != -1) {
                p.getInventory().selectedSlot = slot;
                hasTrident = true;
            }
        }

        if (!hasTrident) return;

        // Apply upward and forward velocity while jump is pressed
        if (mc.options.jumpKey.isPressed()) {
            float yaw = (float) Math.toRadians(p.getYaw());
            float pitch = (float) Math.toRadians(p.getPitch());
            double speed = 2.5;
            double vx = -Math.sin(yaw) * Math.cos(pitch) * speed;
            double vy = -Math.sin(pitch) * speed + 0.5;
            double vz = Math.cos(yaw) * Math.cos(pitch) * speed;
            p.setVelocity(vx, vy, vz);
            p.fallDistance = 0;
        }
    }

    private int findTrident() {
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == Items.TRIDENT) return i;
        }
        return -1;
    }
}
