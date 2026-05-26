package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.ChatUtil;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;

import java.util.HashSet;
import java.util.Set;

public class OreAlert extends Module {

    private final IntSetting range = register(new IntSetting(
            "Range", "Detection radius", 16, 8, 32));

    private final BoolSetting diamond = register(new BoolSetting(
            "Diamond", "Alert on diamond ore", true));

    private final BoolSetting ancient = register(new BoolSetting(
            "Ancient Debris", "Alert on ancient debris", true));

    private final BoolSetting emerald = register(new BoolSetting(
            "Emerald", "Alert on emerald ore", false));

    private final BoolSetting gold = register(new BoolSetting(
            "Gold", "Alert on gold ore", false));

    private final BoolSetting sound = register(new BoolSetting(
            "Sound", "Play sound when new ore detected", true));

    private final BoolSetting chat = register(new BoolSetting(
            "Chat", "Send chat notification when new ore detected", true));

    private final Set<BlockPos> seen = new HashSet<>();
    private int ticker = 0;

    public OreAlert() {
        super("OreAlert", "Alerts when rare ores come into view nearby", Category.WORLD);
    }

    @Override
    public void onEnable() {
        seen.clear();
    }

    @Override
    public void onDisable() {
        seen.clear();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        if (++ticker < 10) return;
        ticker = 0;

        int r = range.get();
        BlockPos center = mc.player.getBlockPos();
        Set<BlockPos> current = new HashSet<>();

        for (BlockPos pos : BlockPos.iterate(center.add(-r, -r, -r), center.add(r, r, r))) {
            var state = mc.world.getBlockState(pos);
            Block block = state.getBlock();
            if (isTrackedOre(block)) {
                current.add(pos.toImmutable());
            }
        }

        for (BlockPos pos : current) {
            if (!seen.contains(pos)) {
                Block block = mc.world.getBlockState(pos).getBlock();
                String oreName = getOreName(block);
                if (oreName != null) {
                    triggerAlert(oreName, pos);
                }
            }
        }

        seen.retainAll(current);
        seen.addAll(current);
    }

    private boolean isTrackedOre(Block block) {
        if (diamond.isEnabled() && (block == Blocks.DIAMOND_ORE || block == Blocks.DEEPSLATE_DIAMOND_ORE)) return true;
        if (ancient.isEnabled() && block == Blocks.ANCIENT_DEBRIS) return true;
        if (emerald.isEnabled() && (block == Blocks.EMERALD_ORE || block == Blocks.DEEPSLATE_EMERALD_ORE)) return true;
        if (gold.isEnabled() && (block == Blocks.GOLD_ORE || block == Blocks.DEEPSLATE_GOLD_ORE || block == Blocks.NETHER_GOLD_ORE)) return true;
        return false;
    }

    private String getOreName(Block block) {
        if (block == Blocks.DIAMOND_ORE || block == Blocks.DEEPSLATE_DIAMOND_ORE) return "Diamond";
        if (block == Blocks.ANCIENT_DEBRIS) return "Ancient Debris";
        if (block == Blocks.EMERALD_ORE || block == Blocks.DEEPSLATE_EMERALD_ORE) return "Emerald";
        if (block == Blocks.GOLD_ORE || block == Blocks.DEEPSLATE_GOLD_ORE || block == Blocks.NETHER_GOLD_ORE) return "Gold";
        return null;
    }

    private void triggerAlert(String oreName, BlockPos pos) {
        if (chat.isEnabled()) {
            ChatUtil.info("OreAlert: " + oreName + " at " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ());
        }
        if (sound.isEnabled() && mc.getSoundManager() != null) {
            mc.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f));
        }
    }
}
