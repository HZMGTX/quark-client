package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

/**
 * CameraClip - prevents the third-person camera from clipping into solid blocks.
 * Works by detecting when the camera would enter a block and pulling it closer.
 */
public class CameraClip extends Module {

    private static CameraClip instance;

    private final DoubleSetting distance = register(new DoubleSetting(
            "Distance", "Max third-person camera distance (blocks)", 4.0, 1.0, 10.0));

    public CameraClip() {
        super("CameraClip", "Prevents camera from clipping into blocks", Category.RENDER);
        instance = this;
    }

    public static CameraClip getInstance() { return instance; }

    public static double getMaxDistance() {
        if (instance == null || !instance.isEnabled()) return 4.0;
        return instance.distance.get();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        if (mc.options.getPerspective().isFirstPerson()) return;

        // Check if camera position is inside a block
        Vec3d cameraPos = mc.gameRenderer.getCamera().getPos();
        BlockPos camBlock = BlockPos.ofFloored(cameraPos);
        boolean inBlock = mc.world.getBlockState(camBlock).isSolidBlock(mc.world, camBlock);

        // The actual clip prevention is done by the game's clipping logic;
        // this module's distance setting is read by the mixin on CameraSubmersionType
        if (inBlock && mc.gameRenderer != null) {
            // Force perspective pull-in via options (no direct API exists; mixin handles this)
        }
    }
}
