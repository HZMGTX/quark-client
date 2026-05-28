package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.module.modules.render.NotificationOverlay;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class OreAlert extends Module {

    private final IntSetting radius = register(new IntSetting("Radius", "Scan radius for ores", 10, 5, 20));
    private final BoolSetting diamond = register(new BoolSetting("Diamond", "Alert for diamond ore", true));
    private final BoolSetting emerald = register(new BoolSetting("Emerald", "Alert for emerald ore", true));
    private final BoolSetting gold = register(new BoolSetting("Gold", "Alert for gold ore", true));
    private final BoolSetting iron = register(new BoolSetting("Iron", "Alert for iron ore", false));
    private final BoolSetting ancient = register(new BoolSetting("Ancient Debris", "Alert for ancient debris", true));
    private final BoolSetting lapis = register(new BoolSetting("Lapis", "Alert for lapis ore", false));
    private final BoolSetting redstone = register(new BoolSetting("Redstone", "Alert for redstone ore", false));

    private final TimerUtil scanTimer = new TimerUtil();
    private final TimerUtil notifTimer = new TimerUtil();
    private final Set<BlockPos> seen = new HashSet<>();

    private int diamondCount;
    private int emeraldCount;
    private int goldCount;
    private int ironCount;
    private int ancientCount;
    private int lapisCount;
    private int redstoneCount;

    public OreAlert() {
        super("OreAlert", "Alerts when valuable ore blocks are found nearby", Category.WORLD);
    }

    @Override
    public void onEnable() {
        seen.clear();
        diamondCount = emeraldCount = goldCount = ironCount = ancientCount = lapisCount = redstoneCount = 0;
    }

    @Override
    public void onDisable() {
        seen.clear();
    }

    @Override
    public String getSuffix() {
        StringBuilder sb = new StringBuilder();
        if (diamond.isEnabled() && diamondCount > 0) sb.append("D:").append(diamondCount).append(" ");
        if (emerald.isEnabled() && emeraldCount > 0) sb.append("E:").append(emeraldCount).append(" ");
        if (gold.isEnabled() && goldCount > 0) sb.append("G:").append(goldCount).append(" ");
        if (iron.isEnabled() && ironCount > 0) sb.append("I:").append(ironCount).append(" ");
        if (ancient.isEnabled() && ancientCount > 0) sb.append("A:").append(ancientCount).append(" ");
        if (lapis.isEnabled() && lapisCount > 0) sb.append("L:").append(lapisCount).append(" ");
        if (redstone.isEnabled() && redstoneCount > 0) sb.append("R:").append(redstoneCount).append(" ");
        return sb.length() > 0 ? sb.toString().trim() : null;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        if (!scanTimer.hasReached(500)) return;
        scanTimer.reset();

        int r = radius.get();
        BlockPos center = mc.player.getBlockPos();
        Set<BlockPos> current = new HashSet<>();
        Map<String, Integer> counts = new HashMap<>();
        Set<String> newOreTypes = new HashSet<>();

        for (BlockPos pos : BlockPos.iterate(center.add(-r, -r, -r), center.add(r, r, r))) {
            Block block = mc.world.getBlockState(pos).getBlock();
            String name = getOreName(block);
            if (name == null) continue;
            BlockPos immutable = pos.toImmutable();
            current.add(immutable);
            counts.merge(name, 1, Integer::sum);
            if (!seen.contains(immutable)) {
                newOreTypes.add(name);
            }
        }

        diamondCount = counts.getOrDefault("Diamond", 0);
        emeraldCount = counts.getOrDefault("Emerald", 0);
        goldCount = counts.getOrDefault("Gold", 0);
        ironCount = counts.getOrDefault("Iron", 0);
        ancientCount = counts.getOrDefault("Ancient Debris", 0);
        lapisCount = counts.getOrDefault("Lapis", 0);
        redstoneCount = counts.getOrDefault("Redstone", 0);

        if (!newOreTypes.isEmpty() && notifTimer.hasReached(3000)) {
            notifTimer.reset();
            for (String oreName : newOreTypes) {
                int count = counts.getOrDefault(oreName, 0);
                NotificationOverlay.send("OreAlert", oreName + " x" + count + " nearby!", NotificationOverlay.NotifType.INFO);
            }
        }

        seen.retainAll(current);
        seen.addAll(current);
    }

    private String getOreName(Block block) {
        if (diamond.isEnabled() && (block == Blocks.DIAMOND_ORE || block == Blocks.DEEPSLATE_DIAMOND_ORE)) return "Diamond";
        if (emerald.isEnabled() && (block == Blocks.EMERALD_ORE || block == Blocks.DEEPSLATE_EMERALD_ORE)) return "Emerald";
        if (gold.isEnabled() && (block == Blocks.GOLD_ORE || block == Blocks.DEEPSLATE_GOLD_ORE || block == Blocks.NETHER_GOLD_ORE)) return "Gold";
        if (iron.isEnabled() && (block == Blocks.IRON_ORE || block == Blocks.DEEPSLATE_IRON_ORE)) return "Iron";
        if (ancient.isEnabled() && block == Blocks.ANCIENT_DEBRIS) return "Ancient Debris";
        if (lapis.isEnabled() && (block == Blocks.LAPIS_ORE || block == Blocks.DEEPSLATE_LAPIS_ORE)) return "Lapis";
        if (redstone.isEnabled() && (block == Blocks.REDSTONE_ORE || block == Blocks.DEEPSLATE_REDSTONE_ORE)) return "Redstone";
        return null;
    }
}
