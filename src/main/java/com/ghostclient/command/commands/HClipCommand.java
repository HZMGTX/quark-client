package com.ghostclient.command.commands;

import com.ghostclient.command.Command;
import com.ghostclient.util.ChatUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Vec3d;

public class HClipCommand extends Command {

    public HClipCommand() {
        super("hclip", "Teleport horizontally in facing direction. Usage: .hclip <distance>");
    }

    @Override
    public void execute(String[] args) {
        if (args.length < 1) { ChatUtil.error("Usage: .hclip <distance>"); return; }
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.getNetworkHandler() == null) return;
        try {
            double dist = Double.parseDouble(args[0]);
            Vec3d look = mc.player.getRotationVector();
            Vec3d pos = mc.player.getPos();
            double nx = pos.x + look.x * dist;
            double nz = pos.z + look.z * dist;
            mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(nx, pos.y, nz, false));
            mc.player.setPos(nx, pos.y, nz);
            ChatUtil.success("Clipped " + dist + " blocks horizontally.");
        } catch (NumberFormatException e) {
            ChatUtil.error("Invalid distance: " + args[0]);
        }
    }
}
