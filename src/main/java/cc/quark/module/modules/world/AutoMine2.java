package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.setting.StringSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.block.Block;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.registry.Registries;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

/**
 * AutoMine2 - Automatically mines blocks matching a configurable target type
 * within a search radius around the player.
 */
public class AutoMine2 extends Module {

    private final StringSetting target = register(new StringSetting(
            "Target", "Block to mine (registry name, e.g. coal_ore)", "coal_ore"));
    private final IntSetting radius = register(new IntSetting(
            "Radius", "Search radius around player", 4, 1, 8));
    private final IntSetting delay = register(new IntSetting(
            "Delay", "Milliseconds between mine actions", 50, 0, 500));
    private final BoolSetting autoTool = register(new BoolSetting(
            "AutoTool", "Switch to best tool for the target block", true));
    private final BoolSetting packetMine = register(new BoolSetting(
            "PacketMine", "Send START/STOP packets for faster mining", true));

    private final TimerUtil timer = new TimerUtil();
    private BlockPos currentTarget = null;
    private int prevSlot = 0;

    public AutoMine2() {
        super("AutoMine2", "Automatically mines blocks matching a configurable target type", Category.WORLD);
    }

    @Override
    public void onEnable() {
        currentTarget = null;
        timer.reset();
    }

    @Override
    public void onDisable() {
        if (mc.player != null && prevSlot >= 0 && prevSlot < 9) {
            mc.player.getInventory().selectedSlot = prevSlot;
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(delay.get())) return;

        Block targetBlock = resolveBlock(target.get());
        if (targetBlock == null) return;

        // If current target is already mined, clear it
        if (currentTarget != null && mc.world.getBlockState(currentTarget).isAir()) {
            currentTarget = null;
        }

        // Find nearest target block
        if (currentTarget == null) {
            BlockPos center = mc.player.getBlockPos();
            int r = radius.get();
            double bestDist = Double.MAX_VALUE;
            for (BlockPos pos : BlockPos.iterate(center.add(-r, -r, -r), center.add(r, r, r))) {
                if (!mc.world.getBlockState(pos).isOf(targetBlock)) continue;
                double dist = pos.getSquaredDistance(center);
                if (dist < bestDist) {
                    bestDist = dist;
                    currentTarget = pos.toImmutable();
                }
            }
        }

        if (currentTarget == null) return;

        // Switch to best tool
        if (autoTool.isEnabled()) {
            int best = findBestTool(targetBlock);
            if (best != -1) {
                prevSlot = mc.player.getInventory().selectedSlot;
                mc.player.getInventory().selectedSlot = best;
            }
        }

        Direction face = Direction.UP;

        if (packetMine.isEnabled()) {
            mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(
                    PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, currentTarget, face));
        }

        mc.interactionManager.attackBlock(currentTarget, face);
        mc.player.swingHand(Hand.MAIN_HAND);

        if (mc.world.getBlockState(currentTarget).isAir()) {
            if (packetMine.isEnabled()) {
                mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(
                        PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, currentTarget, face));
            }
            currentTarget = null;
        }

        timer.reset();
    }

    private Block resolveBlock(String name) {
        try {
            String id = name.trim().toLowerCase();
            if (!id.contains(":")) id = "minecraft:" + id;
            return Registries.BLOCK.get(Identifier.of(id));
        } catch (Exception e) {
            return null;
        }
    }

    private int findBestTool(Block block) {
        if (mc.player == null) return -1;
        // Prefer pickaxe for ore/stone, axe for wood, shovel for dirt
        String name = target.get().toLowerCase();
        Class<?> preferred = null;
        if (name.contains("ore") || name.contains("stone") || name.contains("cobble")
                || name.contains("deepslate") || name.contains("granite") || name.contains("diorite")
                || name.contains("andesite") || name.contains("obsidian")) {
            preferred = net.minecraft.item.PickaxeItem.class;
        } else if (name.contains("log") || name.contains("wood") || name.contains("plank")) {
            preferred = net.minecraft.item.AxeItem.class;
        } else if (name.contains("dirt") || name.contains("sand") || name.contains("gravel")
                || name.contains("clay") || name.contains("soul")) {
            preferred = net.minecraft.item.ShovelItem.class;
        }

        for (int i = 0; i < 9; i++) {
            var item = mc.player.getInventory().getStack(i).getItem();
            if (preferred != null && preferred.isInstance(item)) return i;
        }
        // Fallback: any mining tool
        for (int i = 0; i < 9; i++) {
            var item = mc.player.getInventory().getStack(i).getItem();
            if (item instanceof net.minecraft.item.MiningToolItem) return i;
        }
        return -1;
    }
}
