package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.util.math.BlockPos;

public class AntiGlitch extends Module {

    private final BoolSetting unclip = register(new BoolSetting(
            "Unclip", "Teleports player back if they clip into a solid block", true));

    private final BoolSetting noClip = register(new BoolSetting(
            "No Clip", "Prevents player from entering solid blocks (client-side guard)", true));

    private double lastSafeX, lastSafeY, lastSafeZ;
    private boolean hasSafe = false;

    public AntiGlitch() {
        super("AntiGlitch", "Prevents common movement glitches", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        hasSafe = false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        BlockPos pos = mc.player.getBlockPos();
        boolean inSolid = mc.world.getBlockState(pos).isSolidBlock(mc.world, pos);

        if (!inSolid) {
            // Record last safe position
            lastSafeX = mc.player.getX();
            lastSafeY = mc.player.getY();
            lastSafeZ = mc.player.getZ();
            hasSafe = true;
        } else {
            if (unclip.isEnabled() && hasSafe) {
                // Teleport back to last safe position
                mc.player.requestTeleport(lastSafeX, lastSafeY, lastSafeZ);
            }
            if (noClip.isEnabled()) {
                // Nudge upward to escape solid block
                mc.player.setVelocity(mc.player.getVelocity().x, 0.1, mc.player.getVelocity().z);
            }
        }
    }
}
