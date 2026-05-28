package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;

public class AirJump extends Module {

    private final IntSetting count = register(new IntSetting("Extra Jumps", "Number of extra jumps in air", 1, 1, 3));
    private int jumpsLeft;
    private boolean jumpKeyPressed = false;

    public AirJump() {
        super("AirJump", "Jump multiple times in the air", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        jumpsLeft = count.get();
        jumpKeyPressed = false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        if (mc.player.isOnGround() || mc.player.isClimbing()) {
            jumpsLeft = count.get();
        } else if (jumpsLeft > 0 && mc.options.jumpKey.isPressed()) {
            if (!jumpKeyPressed) {
                mc.player.jump();
                mc.player.setVelocity(mc.player.getVelocity().x, 0.42, mc.player.getVelocity().z);
                mc.player.fallDistance = 0;
                jumpsLeft--;
                jumpKeyPressed = true;
            }
        }

        if (!mc.options.jumpKey.isPressed()) {
            jumpKeyPressed = false;
        }
    }
}
