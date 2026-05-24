package com.ghostclient.module.modules.movement;

import com.ghostclient.GhostClient;
import com.ghostclient.event.EventHandler;
import com.ghostclient.event.events.EventTick;
import com.ghostclient.module.Category;
import com.ghostclient.module.Module;
import com.ghostclient.setting.BoolSetting;
import com.ghostclient.setting.DoubleSetting;
import com.ghostclient.setting.ModeSetting;
import net.minecraft.util.math.Vec3d;

public class ElytraFly extends Module {

    private final ModeSetting mode = register(new ModeSetting("Mode", "Flight mode", "Boost", "Boost", "Packet", "Creative"));
    private final DoubleSetting speed = register(new DoubleSetting("Speed", "Fly speed", 5.0, 1.0, 15.0));
    private final BoolSetting autoLaunch = register(new BoolSetting("Auto Launch", "Auto launch with firework", true));
    private boolean fakeElytra = false;

    public ElytraFly() {
        super("ElytraFly", "Fly using elytra mechanics", Category.MOVEMENT, 0);
    }

    @Override
    public void onEnable() {
        GhostClient.getInstance().getEventBus().subscribe(this);
        fakeElytra = mode.getValue().equals("Packet");
    }

    @Override
    public void onDisable() {
        GhostClient.getInstance().getEventBus().unsubscribe(this);
        fakeElytra = false;
        if (mc.player != null) {
            mc.player.getAbilities().allowFlying = false;
            mc.player.getAbilities().flying = false;
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        String m = mode.getValue();

        switch (m) {
            case "Creative" -> {
                mc.player.getAbilities().allowFlying = true;
                mc.player.getAbilities().flying = true;
                double spd = speed.getValue();
                Vec3d vel = mc.player.getVelocity();
                if (mc.options.forwardKey.isPressed())
                    mc.player.setVelocity(vel.x, vel.y, -spd * 0.05);
                if (mc.options.jumpKey.isPressed())
                    mc.player.setVelocity(vel.x, spd * 0.05, vel.z);
                if (mc.options.sneakKey.isPressed())
                    mc.player.setVelocity(vel.x, -spd * 0.05, vel.z);
            }
            case "Boost" -> {
                if (mc.player.isFallFlying()) {
                    Vec3d look = mc.player.getRotationVector();
                    Vec3d vel = mc.player.getVelocity();
                    double spd = speed.getValue() * 0.05;
                    mc.player.setVelocity(
                        vel.x + look.x * spd,
                        vel.y + look.y * spd,
                        vel.z + look.z * spd
                    );
                }
            }
            case "Packet" -> {
                double spd = speed.getValue() * 0.05;
                Vec3d look = mc.player.getRotationVector();
                mc.player.setVelocity(look.x * spd, 0, look.z * spd);
                if (mc.options.jumpKey.isPressed()) mc.player.setVelocity(mc.player.getVelocity().x, spd, mc.player.getVelocity().z);
                if (mc.options.sneakKey.isPressed()) mc.player.setVelocity(mc.player.getVelocity().x, -spd, mc.player.getVelocity().z);
                mc.player.fallDistance = 0;
            }
        }
    }

    public boolean isFakeElytra() { return fakeElytra; }
}
