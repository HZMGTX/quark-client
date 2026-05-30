package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;

/**
 * SlopeClimb - when the player has a horizontal collision and is not on ground,
 * temporarily increases the step height attribute to climb over diagonal terrain.
 * Resets step height when no longer colliding horizontally.
 */
public class SlopeClimb extends Module {

    private static final float VANILLA_STEP = 0.6f;

    private final DoubleSetting maxSlope = register(new DoubleSetting(
            "MaxSlope", "Maximum step height for slope climbing (blocks)", 1.5, 0.6, 2.5));

    private boolean wasColliding = false;

    public SlopeClimb() {
        super("SlopeClimb", "Increases step height when horizontally colliding to climb slopes", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        wasColliding = false;
    }

    @Override
    public void onDisable() {
        if (mc.player != null) setStepHeight(VANILLA_STEP);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        boolean colliding = mc.player.horizontalCollision && !mc.player.isOnGround();

        if (colliding && !wasColliding) {
            // Just started colliding - increase step height
            setStepHeight((float) maxSlope.get());
        } else if (!colliding && wasColliding) {
            // Collision ended - reset step height
            setStepHeight(VANILLA_STEP);
        }

        wasColliding = colliding;
    }

    private void setStepHeight(float height) {
        EntityAttributeInstance attr =
                mc.player.getAttributeInstance(EntityAttributes.GENERIC_STEP_HEIGHT);
        if (attr != null) attr.setBaseValue(height);
    }
}
