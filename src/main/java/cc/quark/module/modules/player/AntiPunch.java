package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventDamage;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

public class AntiPunch extends Module {

    private final DoubleSetting recoil = register(new DoubleSetting(
            "Recoil", "How far to push away from attacker", 0.5, 0.1, 2.0));

    public AntiPunch() {
        super("AntiPunch", "Moves away when punched", Category.PLAYER);
    }

    @EventHandler
    public void onDamage(EventDamage event) {
        if (mc.player == null || mc.world == null) return;

        Entity attacker = event.getSource().getAttacker();
        if (attacker == null) return;

        Vec3d dir = mc.player.getPos().subtract(attacker.getPos()).normalize();
        Vec3d vel = mc.player.getVelocity();
        mc.player.setVelocity(
                vel.x + dir.x * recoil.get(),
                vel.y,
                vel.z + dir.z * recoil.get()
        );
    }
}
