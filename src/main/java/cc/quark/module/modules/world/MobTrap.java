package cc.quark.module.modules.world;

import cc.quark.Quark;
import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.module.setting.BoolSetting;
import cc.quark.module.setting.DoubleSetting;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.stream.Collectors;

public class MobTrap extends Module {

    private final DoubleSetting range = new DoubleSetting("Range", 8, 4, 20);
    private final BoolSetting hostileOnly = new BoolSetting("HostileOnly", true);

    public MobTrap() {
        super("MobTrap", "Highlights mobs that are stuck in traps or water", Category.WORLD);
        addSettings(range, hostileOnly);
    }

    @Override public void onEnable()  { Quark.mc.getEventBus().subscribe(this); }
    @Override public void onDisable() { Quark.mc.getEventBus().unsubscribe(this); }

    @EventHandler
    public void onTick(EventTick event) {
        var mc = Quark.mc;
        if (mc == null || mc.player == null || mc.world == null) return;

        var pos = mc.player.getBlockPos();
        double r = range.get();

        mc.world.getEntitiesByClass(
            hostileOnly.isEnabled() ? HostileEntity.class : net.minecraft.entity.LivingEntity.class,
            mc.player.getBoundingBox().expand(r),
            e -> !e.equals(mc.player)
        ).forEach(e -> {
            if (e.isInFluid() || e.isOnFire()) {
                // Log to chat once per entity per second
                if (e.age % 20 == 0) {
                    String status = e.isOnFire() ? "on fire" : "in fluid";
                    mc.player.sendMessage(
                        net.minecraft.text.Text.literal("[MobTrap] " + e.getType().getName().getString() + " is " + status),
                        true
                    );
                }
            }
        });
    }
}
