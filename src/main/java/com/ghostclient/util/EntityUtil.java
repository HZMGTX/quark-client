package com.ghostclient.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;

/**
 * Utility helpers for entity queries used across combat modules.
 */
public final class EntityUtil {

    private EntityUtil() {}

    /**
     * Returns true when the entity is a living, non-dead entity that can be attacked.
     */
    public static boolean isAlive(Entity entity) {
        return entity instanceof LivingEntity living && !living.isDead() && living.getHealth() > 0f;
    }

    /**
     * Returns true when the entity is an animal (passive mob).
     */
    public static boolean isAnimal(Entity entity) {
        return entity instanceof AnimalEntity;
    }

    /**
     * Returns true when the entity is a hostile mob (not a player, not passive).
     */
    public static boolean isMob(Entity entity) {
        return entity instanceof MobEntity && !(entity instanceof AnimalEntity);
    }

    /**
     * Returns true when the entity is another player (not the local player).
     */
    public static boolean isOtherPlayer(Entity entity) {
        MinecraftClient mc = MinecraftClient.getInstance();
        return entity instanceof PlayerEntity && entity != mc.player;
    }

    /**
     * Distance from the local player's eyes to the entity's eye position.
     */
    public static double distanceTo(Entity entity) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return Double.MAX_VALUE;
        return mc.player.getEyePos().distanceTo(entity.getEyePos());
    }
}
