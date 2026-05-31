package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.ChatUtil;
import cc.quark.util.TimerUtil;
import net.minecraft.client.network.ClientPlayerEntity;

/**
 * AutoEscape — automatically disconnects the player from the server when their
 * health drops to or below a configurable threshold, preventing death and
 * potential item loss.
 */
public class AutoEscape extends Module {

    private final DoubleSetting healthThreshold = register(new DoubleSetting(
            "Health Threshold", "Disconnect when health falls to or below this value (hearts)", 4.0, 1.0, 20.0));

    private final BoolSetting notifyBefore = register(new BoolSetting(
            "Notify Before", "Print a warning message just before disconnecting", true));

    private final IntSetting delayTicks = register(new IntSetting(
            "Delay Ticks", "Ticks to wait between detection and disconnect", 2, 0, 20));

    private final BoolSetting requireOnGround = register(new BoolSetting(
            "Require Ground", "Only escape when on the ground (avoids fall-death race)", false));

    private int ticksLow = 0;
    private boolean hasEscaped = false;

    public AutoEscape() {
        super("AutoEscape", "Auto-disconnects when health is critically low", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        ticksLow = 0;
        hasEscaped = false;
    }

    @Override
    public void onDisable() {
        ticksLow = 0;
        hasEscaped = false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.getNetworkHandler() == null) return;
        if (hasEscaped) return;

        ClientPlayerEntity player = mc.player;

        // Absorb / health cannot go below 0; player.getHealth() returns 0 on death
        if (player.isDead()) return;

        if (requireOnGround.isEnabled() && !player.isOnGround()) {
            ticksLow = 0;
            return;
        }

        float health = player.getHealth();
        if (health <= (float) healthThreshold.get()) {
            ticksLow++;
        } else {
            ticksLow = 0;
            return;
        }

        if (ticksLow >= delayTicks.get()) {
            if (notifyBefore.isEnabled()) {
                ChatUtil.warn("AutoEscape: disconnecting! Health = " + String.format("%.1f", health));
            }
            hasEscaped = true;
            mc.execute(() -> {
                if (mc.getNetworkHandler() != null) {
                    mc.getNetworkHandler().getConnection().disconnect(
                            net.minecraft.text.Text.literal("[AutoEscape] Health critical"));
                }
            });
        }
    }
}
