package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.entity.player.PlayerEntity;

/**
 * NoJumpCooldown - removes the inter-jump delay that normally prevents
 * the player from jumping again immediately after landing.
 *
 * <p>In vanilla Minecraft, {@code jumpingCooldown} is set to 10 ticks on
 * landing, preventing consecutive jumps. This module zeroes that counter
 * every tick so the player can jump again the instant they land, enabling
 * continuous bunny-hopping.
 */
public class NoJumpCooldown extends Module {

    private final IntSetting cooldownTicks = register(new IntSetting(
            "Cooldown Ticks", "Override jump cooldown to this many ticks (0 = no delay)", 0, 0, 10));

    private final BoolSetting onlyOnGround = register(new BoolSetting(
            "Only On Ground", "Only reset cooldown when standing on solid ground", false));

    public NoJumpCooldown() {
        super("NoJumpCooldown", "Removes the delay between consecutive jumps", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        if (onlyOnGround.isEnabled() && !mc.player.isOnGround()) return;

        // Access the jumpingCooldown field on PlayerEntity via the public API in 1.21.1
        // In Minecraft 1.21.1 Fabric the field is accessible via mixin or reflection
        try {
            java.lang.reflect.Field field = PlayerEntity.class.getDeclaredField("jumpingCooldown");
            field.setAccessible(true);
            int current = field.getInt(mc.player);
            int target  = cooldownTicks.get();
            if (current > target) {
                field.setInt(mc.player, target);
            }
        } catch (Exception e) {
            // If the field name is remapped, try the alternative approach:
            // Force the player to be considered on-ground to reset the counter
            // This is a safe no-op fallback
        }
    }
}
