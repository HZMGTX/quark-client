package cc.quark.module.modules.player;

import cc.quark.Quark;
import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.module.setting.IntSetting;
import net.minecraft.util.math.Vec3d;

public class AntiStuck extends Module {

    private final IntSetting stuckTicks = new IntSetting("StuckTicks", 20, 5, 60);

    private Vec3d lastPos;
    private int stuckTimer = 0;

    public AntiStuck() {
        super("AntiStuck", "Detects when movement is blocked and attempts to free the player", Category.PLAYER);
        addSettings(stuckTicks);
    }

    @Override public void onEnable()  { Quark.mc.getEventBus().subscribe(this); timer = 0; lastPos = null; }
    @Override public void onDisable() { Quark.mc.getEventBus().unsubscribe(this); }

    private int timer = 0;

    @EventHandler
    public void onTick(EventTick event) {
        var mc = Quark.mc;
        if (mc == null || mc.player == null) return;
        if (!mc.player.input.playerInput.forward()) { stuckTimer = 0; lastPos = null; return; }

        Vec3d pos = mc.player.getPos();
        if (lastPos != null) {
            double moved = pos.squaredDistanceTo(lastPos.x, pos.y, lastPos.z);
            if (moved < 0.001) {
                stuckTimer++;
                if (stuckTimer >= stuckTicks.get()) {
                    mc.player.jump();
                    stuckTimer = 0;
                }
            } else {
                stuckTimer = 0;
            }
        }
        lastPos = pos;
    }
}
