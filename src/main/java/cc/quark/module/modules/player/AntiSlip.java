package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.BlockPos;

/**
 * AntiSlip — prevents the player from slipping on ice, blue ice, packed ice,
 * and slime by reducing their velocity when standing on those blocks.
 */
public class AntiSlip extends Module {

    private final BoolSetting affectIce = register(new BoolSetting(
            "Ice", "Prevent slipping on regular ice", true));

    private final BoolSetting affectPackedIce = register(new BoolSetting(
            "Packed Ice", "Prevent slipping on packed ice", true));

    private final BoolSetting affectBlueIce = register(new BoolSetting(
            "Blue Ice", "Prevent slipping on blue ice", true));

    private final BoolSetting affectSlime = register(new BoolSetting(
            "Slime", "Prevent slipping on slime blocks", false));

    private final DoubleSetting dampening = register(new DoubleSetting(
            "Dampening", "How aggressively to damp the slip (0=none, 1=full stop)", 0.8, 0.0, 1.0));

    public AntiSlip() {
        super("AntiSlip", "Prevents slipping on ice and slime", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        ClientPlayerEntity player = mc.player;

        if (!player.isOnGround()) return;

        BlockPos below = player.getBlockPos().down();
        Block block = mc.world.getBlockState(below).getBlock();

        boolean isSlippery = false;
        if (affectIce.isEnabled() && block == Blocks.ICE) isSlippery = true;
        if (affectPackedIce.isEnabled() && block == Blocks.PACKED_ICE) isSlippery = true;
        if (affectBlueIce.isEnabled() && block == Blocks.BLUE_ICE) isSlippery = true;
        if (affectSlime.isEnabled() && block == Blocks.SLIME_BLOCK) isSlippery = true;

        if (!isSlippery) return;

        double damp = dampening.get();
        // Reduce horizontal velocity proportionally
        double newVX = player.getVelocity().x * (1.0 - damp);
        double newVZ = player.getVelocity().z * (1.0 - damp);
        player.setVelocity(newVX, player.getVelocity().y, newVZ);
    }
}
