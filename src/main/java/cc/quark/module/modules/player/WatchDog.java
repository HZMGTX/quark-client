package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.entity.player.PlayerEntity;

public class WatchDog extends Module {

    private final BoolSetting xray = register(new BoolSetting(
            "XRay", "Monitor for players using XRay behavior", true));

    private final BoolSetting fly = register(new BoolSetting(
            "Fly", "Monitor for players flying illegally", true));

    public WatchDog() {
        super("WatchDog", "Monitors suspicious activity and alerts", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        for (var entity : mc.world.getEntities()) {
            if (!(entity instanceof PlayerEntity p)) continue;
            if (p == mc.player) continue;

            if (fly.isEnabled()) {
                // Detect sustained non-falling, non-ground movement
                boolean suspiciousFly = !p.isOnGround()
                        && !p.isFallFlying()
                        && !p.isTouchingWater()
                        && Math.abs(p.getVelocity().y) < 0.01;
                if (suspiciousFly) {
                    mc.player.sendMessage(
                            net.minecraft.text.Text.literal("[WatchDog] " + p.getName().getString() + " may be flying!"),
                            true
                    );
                }
            }
        }
    }
}
