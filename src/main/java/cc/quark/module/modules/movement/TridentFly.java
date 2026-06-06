package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

public class TridentFly extends Module {
    private final DoubleSetting speed = register(new DoubleSetting("Speed", "Fly speed", 1.5, 0.5, 5.0));

    public TridentFly() { super("TridentFly", "Fly using trident Riptide enchantment", Category.MOVEMENT); }

    @EventHandler
    public void onTick(EventTick e) {
        if (mc.player == null) return;
        boolean hasTrident = false;
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == Items.TRIDENT) { hasTrident = true; break; }
        }
        if (!hasTrident) return;
        double yaw = Math.toRadians(mc.player.getYaw());
        double pitch = Math.toRadians(mc.player.getPitch());
        double s = speed.get();
        double x = -Math.sin(yaw) * Math.cos(pitch) * s;
        double y = -Math.sin(pitch) * s;
        double z = Math.cos(yaw) * Math.cos(pitch) * s;
        mc.player.setVelocity(x, y, z);
    }
}
