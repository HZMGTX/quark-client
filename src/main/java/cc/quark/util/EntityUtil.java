package cc.quark.util;

import cc.quark.Quark;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class EntityUtil {

    private EntityUtil() {}

    public static boolean isAlive(Entity entity) {
        return entity instanceof LivingEntity living && !living.isRemoved() && living.getHealth() > 0f;
    }

    public static boolean isAnimal(Entity entity) {
        return entity instanceof AnimalEntity;
    }

    public static boolean isMob(Entity entity) {
        return entity instanceof MobEntity && !(entity instanceof AnimalEntity);
    }

    public static boolean isOtherPlayer(Entity entity) {
        MinecraftClient mc = MinecraftClient.getInstance();
        return entity instanceof PlayerEntity && entity != mc.player;
    }

    public static double distanceTo(Entity entity) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return Double.MAX_VALUE;
        return mc.player.getEyePos().distanceTo(entity.getEyePos());
    }

    @SuppressWarnings("unchecked")
    public static <T extends LivingEntity> List<T> getEntitiesOfType(Class<T> type, double range) {
        MinecraftClient mc = MinecraftClient.getInstance();
        List<T> result = new ArrayList<>();
        if (mc.world == null || mc.player == null) return result;
        for (Entity entity : mc.world.getEntities()) {
            if (!type.isInstance(entity)) continue;
            if (entity == mc.player) continue;
            LivingEntity living = (LivingEntity) entity;
            if (living.isRemoved() || living.getHealth() <= 0f) continue;
            if (distanceTo(entity) <= range) result.add((T) entity);
        }
        return result;
    }

    public static boolean hasLineOfSight(Entity from, Entity to) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null) return false;
        Vec3d start = from.getEyePos();
        Vec3d end   = to.getEyePos();
        RaycastContext ctx = new RaycastContext(start, end,
                RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.NONE, from);
        HitResult result = mc.world.raycast(ctx);
        return result.getType() == HitResult.Type.MISS;
    }

    public static double getAngleTo(Entity entity) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return 180.0;
        Vec3d eyes      = mc.player.getEyePos();
        Vec3d look      = mc.player.getRotationVec(1.0f);
        Vec3d toEntity  = entity.getEyePos().subtract(eyes).normalize();
        double dot = look.dotProduct(toEntity);
        dot = Math.max(-1.0, Math.min(1.0, dot));
        return Math.toDegrees(Math.acos(dot));
    }

    public static List<LivingEntity> getSortedByAngle(double range) {
        MinecraftClient mc = MinecraftClient.getInstance();
        List<LivingEntity> result = new ArrayList<>();
        if (mc.world == null || mc.player == null) return result;
        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player) continue;
            if (!(entity instanceof LivingEntity living)) continue;
            if (living.isRemoved() || living.getHealth() <= 0f) continue;
            if (distanceTo(entity) > range) continue;
            result.add(living);
        }
        result.sort(Comparator.comparingDouble(EntityUtil::getAngleTo));
        return result;
    }

    public static boolean isFriend(Entity entity) {
        if (!(entity instanceof PlayerEntity player)) return false;
        if (Quark.getInstance() == null) return false;
        return Quark.getInstance().getFriendManager().isFriend(player.getGameProfile().getName());
    }
}
