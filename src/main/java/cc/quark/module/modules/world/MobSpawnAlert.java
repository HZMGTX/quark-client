package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.util.ChatUtil;
import cc.quark.util.TimerUtil;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.mob.PhantomEntity;
import net.minecraft.entity.player.PlayerEntity;

public class MobSpawnAlert extends Module {
    private final DoubleSetting range = register(new DoubleSetting("Range", "Alert range in blocks", 16.0, 5.0, 50.0));
    private final BoolSetting creepers = register(new BoolSetting("Creepers", "Alert for creepers", true));
    private final BoolSetting skeletons = register(new BoolSetting("Skeletons", "Alert for skeletons", true));
    private final BoolSetting phantoms = register(new BoolSetting("Phantoms", "Alert for phantoms", true));
    private final BoolSetting hostilePlayers = register(new BoolSetting("Players", "Alert for nearby players", false));

    private final TimerUtil timer = new TimerUtil();

    public MobSpawnAlert() {
        super("Mob Alert", "Alerts when dangerous mobs spawn nearby", Category.WORLD, 0);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        if (!timer.hasReached(2000)) return;
        timer.reset();

        double r = range.get();
        for (var entity : mc.world.getEntities()) {
            if (entity.squaredDistanceTo(mc.player) > r * r) continue;
            String name = null;
            if (creepers.isEnabled() && entity instanceof CreeperEntity) name = "§aCREEPER";
            else if (skeletons.isEnabled() && entity instanceof SkeletonEntity) name = "§fSKELETON";
            else if (phantoms.isEnabled() && entity instanceof PhantomEntity) name = "§5PHANTOM";
            else if (hostilePlayers.isEnabled() && entity instanceof PlayerEntity p && p != mc.player) name = "§cPLAYER §f" + p.getName().getString();

            if (name != null) {
                int dist = (int) mc.player.distanceTo(entity);
                ChatUtil.warn("[MobAlert] " + name + " §7at §f" + dist + "m");
            }
        }
    }
}
