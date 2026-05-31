package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.ModeSetting;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;

/**
 * StepSounds — plays custom step sounds when the player is walking on the ground.
 * The vanilla step sounds are replaced or supplemented with a selectable sound.
 */
public class StepSounds extends Module {

    private final ModeSetting sound = register(new ModeSetting(
            "Sound", "Sound to play on each step",
            "Gravel", "Gravel", "Wood", "Stone", "Sand", "Grass", "Snow"));

    private final DoubleSetting volume = register(new DoubleSetting(
            "Volume", "Step sound volume", 0.5, 0.1, 2.0));

    private final DoubleSetting pitch = register(new DoubleSetting(
            "Pitch", "Step sound pitch", 1.0, 0.5, 2.0));

    private final BoolSetting onlyOnGround = register(new BoolSetting(
            "Only On Ground", "Only play sounds when on the ground", true));

    private double prevX;
    private double prevZ;
    private boolean wasStepping;
    // Accumulate horizontal distance between sound plays
    private double distAccum;
    private static final double STEP_INTERVAL = 0.5; // blocks per step sound

    public StepSounds() {
        super("StepSounds", "Plays custom step sounds when walking", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        if (mc.player != null) {
            prevX = mc.player.getX();
            prevZ = mc.player.getZ();
        }
        distAccum = 0;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        ClientPlayerEntity player = mc.player;

        if (onlyOnGround.isEnabled() && !player.isOnGround()) {
            prevX = player.getX();
            prevZ = player.getZ();
            wasStepping = false;
            return;
        }

        double dx = player.getX() - prevX;
        double dz = player.getZ() - prevZ;
        double moved = Math.sqrt(dx * dx + dz * dz);
        prevX = player.getX();
        prevZ = player.getZ();

        // Only play if moving meaningfully
        if (moved < 0.01) {
            wasStepping = false;
            return;
        }

        distAccum += moved;
        if (distAccum < STEP_INTERVAL) return;
        distAccum -= STEP_INTERVAL;
        wasStepping = true;

        playStepSound(player);
    }

    private void playStepSound(ClientPlayerEntity player) {
        net.minecraft.sound.SoundEvent soundEvent = switch (sound.get()) {
            case "Wood"   -> SoundEvents.BLOCK_WOOD_STEP;
            case "Stone"  -> SoundEvents.BLOCK_STONE_STEP;
            case "Sand"   -> SoundEvents.BLOCK_SAND_STEP;
            case "Grass"  -> SoundEvents.BLOCK_GRASS_STEP;
            case "Snow"   -> SoundEvents.BLOCK_SNOW_STEP;
            default       -> SoundEvents.BLOCK_GRAVEL_STEP;
        };

        mc.world.playSound(
                player.getX(),
                player.getY(),
                player.getZ(),
                soundEvent,
                SoundCategory.PLAYERS,
                (float) volume.get(),
                (float) pitch.get(),
                false);
    }
}
