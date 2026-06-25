package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;

public class WTap3 extends Module {

    private final IntSetting durationMs = register(new IntSetting(
            "Duration Ms", "How long to release W key in milliseconds", 50, 10, 200));

    private final BoolSetting autoSync = register(new BoolSetting(
            "Auto Sync", "Synchronize W-tap timing with attack cooldown", true));

    private final TimerUtil tapTimer = new TimerUtil();
    private boolean tapping = false;

    public WTap3() {
        super("WTap3", "Performs W-tap (briefly releases forward) between attacks", Category.COMBAT);
    }

    @Override
    public void onDisable() {
        if (mc.options != null && tapping) {
            mc.options.forwardKey.setPressed(false);
        }
        tapping = false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        if (tapping) {
            if (tapTimer.hasReached(durationMs.get())) {
                mc.options.forwardKey.setPressed(false);
                tapping = false;
            }
            return;
        }

        // Only W-tap when sprinting and moving forward
        if (!mc.player.isSprinting()) return;
        if (mc.player.input.movementForward <= 0) return;

        // Auto-sync: only tap right after an attack (cooldown just reset)
        if (autoSync.isEnabled()) {
            float cooldown = mc.player.getAttackCooldownProgress(0.0f);
            // Tap when cooldown is very high (about to be full), right after an attack
            if (cooldown < 0.85f) return;
        }

        // Perform W-tap
        mc.player.setSprinting(false);
        mc.options.forwardKey.setPressed(false);
        tapping = true;
        tapTimer.reset();
    }
}
