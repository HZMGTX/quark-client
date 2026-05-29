package cc.quark.module.modules.render;

import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * ItemPhysics — makes dropped item entities visually tumble/rotate.
 *
 * The actual per-entity rotation is applied via a mixin hook that calls
 * {@link #getRotation(UUID)} to look up the current angle for each item.
 * If no mixin is present the settings are still exposed for future integration.
 */
public class ItemPhysics extends Module {

    public static ItemPhysics INSTANCE;

    private final DoubleSetting rotSpeed = register(new DoubleSetting(
            "Rotation Speed", "Degrees per tick the item tumbles", 3.0, 0.5, 20.0));
    private final BoolSetting   randomAxis = register(new BoolSetting(
            "Random Axis", "Each item tumbles on a random axis", true));

    /** Per-entity accumulated rotation angle (degrees). */
    private final Map<UUID, float[]> rotations = new HashMap<>();

    public ItemPhysics() {
        super("ItemPhysics", "Makes dropped items tumble/rotate visually (mixin-assisted)", Category.RENDER);
        INSTANCE = this;
    }

    @Override
    public void onDisable() {
        rotations.clear();
    }

    /**
     * Returns the current [yaw, pitch, roll] rotation for the given item entity UUID,
     * advancing the angle by rotSpeed each call.
     * Called from the mixin on every render frame.
     */
    public float[] getRotation(UUID id) {
        float[] rot = rotations.computeIfAbsent(id, k -> new float[]{
                randomAxis.isEnabled() ? (float)(Math.random() * 360) : 0f,
                randomAxis.isEnabled() ? (float)(Math.random() * 360) : 0f,
                0f
        });
        float speed = (float) rotSpeed.get();
        rot[0] = (rot[0] + speed) % 360f;
        rot[1] = (rot[1] + speed * 0.7f) % 360f;
        return rot;
    }

    public double getRotSpeed() {
        return rotSpeed.get();
    }
}
