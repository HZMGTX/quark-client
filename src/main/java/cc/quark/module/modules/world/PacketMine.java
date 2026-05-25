package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import net.minecraft.block.Blocks;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class PacketMine extends Module {

    private final IntSetting packets = register(new IntSetting(
            "Packets", "Extra START packets to send per tick", 3, 1, 10));

    public PacketMine() {
        super("PacketMine", "Sends extra mining packets to dramatically speed up block breaking", Category.WORLD);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        if (!mc.options.attackKey.isPressed()) return;

        HitResult hit = mc.crosshairTarget;
        if (hit == null || hit.getType() != HitResult.Type.BLOCK) return;

        BlockHitResult bhr = (BlockHitResult) hit;
        BlockPos pos  = bhr.getBlockPos();
        Direction face = bhr.getSide();

        if (mc.world.getBlockState(pos).isAir()) return;
        if (mc.world.getBlockState(pos).getBlock() == Blocks.BEDROCK) return;

        for (int i = 0; i < packets.get(); i++) {
            mc.player.networkHandler.sendPacket(
                    new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, pos, face));
        }
    }
}
