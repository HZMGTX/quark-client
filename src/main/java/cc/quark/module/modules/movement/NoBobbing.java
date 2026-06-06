package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;

/**
 * NoBobbing - removes view and hand bobbing without affecting movement.
 *
 * Minecraft's bobbing is driven by {@code PlayerEntity#prevStrideDistance} and
 * related stride fields. By zeroing those each tick we eliminate the bob
 * while leaving velocity entirely untouched.
 */
public class NoBobbing extends Module {

    private final BoolSetting viewBob = register(new BoolSetting(
            "View Bob", "Remove camera bobbing", true));

    private final BoolSetting handBob = register(new BoolSetting(
            "Hand Bob", "Remove hand swinging / sway bob", true));

    // Saved vanilla bobbing setting so we can restore it on disable
    private boolean savedBobView = false;

    public NoBobbing() {
        super("NoBobbing", "Removes camera and hand bobbing without affecting movement", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        mc.getEventBus().subscribe(this);
        if (mc.options != null) {
            savedBobView = mc.options.getBobView().getValue();
            if (viewBob.isEnabled()) {
                mc.options.getBobView().setValue(false);
            }
        }
    }

    @Override
    public void onDisable() {
        mc.getEventBus().unsubscribe(this);
        // Restore vanilla bobbing option
        if (mc.options != null) {
            mc.options.getBobView().setValue(savedBobView);
        }
        // Reset stride fields so bob doesn't snap when re-enabled
        if (mc.player != null) {
            mc.player.strideDistance = 0f;
            mc.player.prevStrideDistance = 0f;
            mc.player.nextStepSoundDistance = 0f;
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        // Keep bobbing options in sync with setting toggles changed at runtime
        if (mc.options != null && viewBob.isEnabled()) {
            mc.options.getBobView().setValue(false);
        }

        if (handBob.isEnabled()) {
            // Zeroing stride distance prevents the hand bob animation
            mc.player.prevStrideDistance = 0f;
            mc.player.strideDistance     = 0f;
        }
    }
}
