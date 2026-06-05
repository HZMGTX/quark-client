package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

public class TargetStrafe extends Module {
    private final DoubleSetting speed = register(new DoubleSetting("Speed", "Strafe speed", 0.5, 0.1, 2.0));
    private final BoolSetting onlyPlayers = register(new BoolSetting("Only Players", "Only strafe around players", true));
    private int direction = 1;
    private int dirTimer = 0;

    public TargetStrafe() { super("TargetStrafe", "Strafes around your target for better combat", Category.COMBAT); }
    @Override public void onEnable() { mc.getEventBus().subscribe(this); }
    @Override public void onDisable() { mc.getEventBus().unsubscribe(this); }

    @EventHandler
    public void onTick(EventTick e) {
        if (mc.player == null || mc.world == null) return;
        LivingEntity target = null;
        double closest = 6.0;
        for (var ent : mc.world.getEntities()) {
            if (onlyPlayers.isEnabled() && !(ent instanceof PlayerEntity)) continue;
            if (!(ent instanceof LivingEntity le)) continue;
            if (ent == mc.player) continue;
            double dist = mc.player.distanceTo(le);
            if (dist < closest) { closest = dist; target = le; }
        }
        if (target == null) return;
        dirTimer++;
        if (dirTimer > 40) { direction = -direction; dirTimer = 0; }
        double angle = Math.atan2(target.getZ() - mc.player.getZ(), target.getX() - mc.player.getX());
        angle += Math.PI / 2 * direction;
        double s = speed.get();
        mc.player.setVelocity(Math.cos(angle) * s, mc.player.getVelocity().y, Math.sin(angle) * s);
    }
}
