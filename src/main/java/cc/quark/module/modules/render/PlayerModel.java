package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.client.option.PlayerModelPart;

/**
 * PlayerModel — alters the local player's visible model layers (cape, jacket,
 * sleeves, pants legs, hat) through the vanilla PlayerModelPart options.
 *
 * These are client-side skin layer toggles identical to what the player can
 * set in the vanilla Options → Skin Customization screen.
 */
public class PlayerModel extends Module {

    private final BoolSetting showCape = register(new BoolSetting(
            "Show Cape", "Toggle cape rendering", true));

    private final BoolSetting showJacket = register(new BoolSetting(
            "Show Jacket", "Toggle outer jacket layer", true));

    private final BoolSetting showLeftSleeve = register(new BoolSetting(
            "Left Sleeve", "Toggle left sleeve layer", true));

    private final BoolSetting showRightSleeve = register(new BoolSetting(
            "Right Sleeve", "Toggle right sleeve layer", true));

    private final BoolSetting showLeftPants = register(new BoolSetting(
            "Left Pants", "Toggle left pants layer", true));

    private final BoolSetting showRightPants = register(new BoolSetting(
            "Right Pants", "Toggle right pants layer", true));

    private final BoolSetting showHat = register(new BoolSetting(
            "Hat Layer", "Toggle hat/second-head layer", true));

    /** Bitmask of model parts that were enabled before this module was turned on. */
    private int originalMask = -1;

    public PlayerModel() {
        super("PlayerModel", "Changes your player model appearance settings", Category.RENDER);
    }

    @Override
    public void onEnable() {
        originalMask = getCurrentMask();
    }

    @Override
    public void onDisable() {
        if (mc.options == null) return;
        if (originalMask < 0) return;
        // Restore all parts that were originally enabled
        for (PlayerModelPart part : PlayerModelPart.values()) {
            boolean wasEnabled = (originalMask & part.getBitFlag()) != 0;
            setPartEnabled(part, wasEnabled);
        }
        originalMask = -1;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.options == null) return;
        setPartEnabled(PlayerModelPart.CAPE, showCape.isEnabled());
        setPartEnabled(PlayerModelPart.JACKET, showJacket.isEnabled());
        setPartEnabled(PlayerModelPart.LEFT_SLEEVE, showLeftSleeve.isEnabled());
        setPartEnabled(PlayerModelPart.RIGHT_SLEEVE, showRightSleeve.isEnabled());
        setPartEnabled(PlayerModelPart.LEFT_PANTS_LEG, showLeftPants.isEnabled());
        setPartEnabled(PlayerModelPart.RIGHT_PANTS_LEG, showRightPants.isEnabled());
        setPartEnabled(PlayerModelPart.HAT, showHat.isEnabled());
    }

    /**
     * Enable or disable a model part. Uses the same bitmask mechanism as
     * vanilla {@code GameOptions.skinModelParts}.
     */
    private void setPartEnabled(PlayerModelPart part, boolean enable) {
        boolean current = mc.options.isPlayerModelPartEnabled(part);
        if (current == enable) return;
        // togglePlayerModelPart(part, enable) is available in 1.20+
        mc.options.togglePlayerModelPart(part, enable);
    }

    private int getCurrentMask() {
        if (mc.options == null) return 0;
        int mask = 0;
        for (PlayerModelPart part : PlayerModelPart.values()) {
            if (mc.options.isPlayerModelPartEnabled(part)) {
                mask |= part.getBitFlag();
            }
        }
        return mask;
    }
}
