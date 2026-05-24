package cc.quark.command.commands;

import cc.quark.command.Command;
import cc.quark.util.ChatUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Vec3d;

public class VClipCommand extends Command {

    public VClipCommand() {
        super("vclip", "Teleport vertically. Usage: .vclip <distance>");
    }

    @Override
    public void execute(String[] args) {
        if (args.length < 1) { ChatUtil.error("Usage: .vclip <distance>"); return; }
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.getNetworkHandler() == null) return;
        try {
            double dist = Double.parseDouble(args[0]);
            Vec3d pos = mc.player.getPos();
            mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(pos.x, pos.y + dist, pos.z, false));
            mc.player.setPos(pos.x, pos.y + dist, pos.z);
            ChatUtil.success("Clipped " + dist + " blocks vertically.");
        } catch (NumberFormatException e) {
            ChatUtil.error("Invalid distance: " + args[0]);
        }
    }
}
