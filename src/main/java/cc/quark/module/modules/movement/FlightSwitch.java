package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventKey;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.setting.ModeSetting;
import net.minecraft.util.math.Vec3d;

/**
 * FlightSwitch - cycles between different flight modes (Vanilla creative fly,
 * Elytra/fall-fly, and a simple Packet-based hover) with a configurable hotkey.
 *
 * <p>Modes:
 * <ul>
 *   <li><b>Vanilla</b>  - toggles the player's creative-fly ability flag.</li>
 *   <li><b>Creative</b> - same as Vanilla; included as an explicit alias.</li>
 *   <li><b>Packet</b>   - sets near-zero Y velocity each tick to hover in place.</li>
 * </ul>
 */
public class FlightSwitch extends Module {

    private final ModeSetting mode = register(new ModeSetting(
            "Mode", "Flight mode to activate", "Vanilla", "Vanilla", "Creative", "Packet"));

    private final IntSetting keybind = register(new IntSetting(
            "Keybind", "GLFW key code to switch mode (0 = disabled)", 0, 0, 348));

    public FlightSwitch() {
        super("FlightSwitch", "Switch between fly modes quickly", Category.MOVEMENT);
    }

    @EventHandler
    public void onKey(EventKey event) {
        if (keybind.get() == 0) return;
        if (event.getKey() != keybind.get()) return;

        mode.cycle();
        applyMode();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        applyMode();
    }

    private void applyMode() {
        if (mc.player == null) return;

        switch (mode.get()) {
            case "Vanilla":
            case "Creative": {
                mc.player.getAbilities().allowFlying = true;
                mc.player.getAbilities().flying = true;
                mc.player.sendAbilitiesUpdate();
                break;
            }
            case "Packet": {
                // Zero out vertical velocity to hover
                Vec3d vel = mc.player.getVelocity();
                if (Math.abs(vel.y) > 0.02) {
                    mc.player.setVelocity(vel.x, 0, vel.z);
                }
                mc.player.fallDistance = 0;
                break;
            }
        }
    }

    @Override
    public void onDisable() {
        if (mc.player == null) return;
        if (!mc.player.isCreative() && !mc.player.isSpectator()) {
            mc.player.getAbilities().allowFlying = false;
            mc.player.getAbilities().flying = false;
            mc.player.sendAbilitiesUpdate();
        }
    }

    @Override
    public String getSuffix() {
        return mode.get();
    }
}
