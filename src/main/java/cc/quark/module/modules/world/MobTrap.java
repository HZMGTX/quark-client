package cc.quark.module.modules.world;

import cc.quark.Quark;
import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.stream.Collectors;

public class MobTrap extends Module {

    private final DoubleSetting range = register(new DoubleSetting("Range", "Range", 8, 4, 20));
    private final BoolSetting hostileOnly = register(new BoolSetting("HostileOnly", "HostileOnly", true));

    public MobTrap() {
        super("MobTrap", "Highlights mobs that are stuck in traps or water", Category.WORLD);
    }


    @EventHandler
    public void onTick(EventTick event) {
        
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
