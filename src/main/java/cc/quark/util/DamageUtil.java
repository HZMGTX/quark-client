package cc.quark.util;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;

public class DamageUtil {
    public static final float CRYSTAL_RADIUS = 6.0f;

    // Vanilla explosion exposure formula (simplified)
    public static float getExplosionDamage(Vec3d explosionPos, LivingEntity entity) {
        double dist = entity.getPos().distanceTo(explosionPos);
        if (dist > CRYSTAL_RADIUS * 2) return 0f;
        double exposure = 1.0 - (dist / (CRYSTAL_RADIUS * 2.0));
        float impact = (float)(exposure * exposure);
        float baseDamage = (impact + exposure) * 6f * 0.85f / 2f;
        // simplified armor reduction: 20 armor = 80% reduction cap
        float armor = entity.getArmor();
        float reduction = 1f - Math.min(0.8f, armor * 0.04f);
        return baseDamage * reduction;
    }

    public static float getSelfDamage(Vec3d explosionPos) {
        var mc = net.minecraft.client.MinecraftClient.getInstance();
        if (mc.player == null) return 0f;
        return getExplosionDamage(explosionPos, mc.player);
    }
}
