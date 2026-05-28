package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import net.minecraft.client.gui.screen.DeathScreen;

/**
 * AutoRespawn - automatically respawns the player when the death screen appears.
 */
public class AutoRespawn extends Module {

    private final IntSetting delaySetting = register(new IntSetting(
            "Delay", "Ticks to wait before respawning", 20, 0, 100));

    private boolean isDead = false;
    private int delayTicks = 0;

    public AutoRespawn() {
        super("AutoRespawn", "Automatically clicks respawn on the death screen", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        isDead = false;
        delayTicks = 0;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        boolean dead = mc.player.isDead() || mc.player.getHealth() <= 0
                || mc.currentScreen instanceof DeathScreen;

        if (dead && !isDead) {
            isDead = true;
            delayTicks = delaySetting.get();
        }

        if (isDead) {
            if (delayTicks > 0) {
                delayTicks--;
                return;
            }
            // Respawn
            mc.player.requestRespawn();
            if (mc.currentScreen instanceof DeathScreen) {
                mc.setScreen(null);
            }
            isDead = false;
            delayTicks = 0;
        }
    }
}
