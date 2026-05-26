package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender3D;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.RenderUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;

import java.util.ArrayList;
import java.util.List;

public class XRay extends Module {

    private final BoolSetting diamond  = register(new BoolSetting("Diamond",  "Show diamond ore",          true));
    private final BoolSetting gold     = register(new BoolSetting("Gold",     "Show gold ore",              true));
    private final BoolSetting iron     = register(new BoolSetting("Iron",     "Show iron ore",              true));
    private final BoolSetting emerald  = register(new BoolSetting("Emerald",  "Show emerald ore",           true));
    private final BoolSetting debris   = register(new BoolSetting("Debris",   "Show ancient debris",        true));
    private final BoolSetting coal     = register(new BoolSetting("Coal",     "Show coal ore",              false));
    private final BoolSetting lapis    = register(new BoolSetting("Lapis",    "Show lapis ore",             true));
    private final BoolSetting redstone = register(new BoolSetting("Redstone", "Show redstone ore",          true));
    private final BoolSetting copper   = register(new BoolSetting("Copper",   "Show copper ore",            false));
    private final BoolSetting quartz   = register(new BoolSetting("Quartz",   "Show nether quartz ore",     false));
    private final IntSetting  scanRange= register(new IntSetting("Range",     "Scan radius in chunks",      3, 1, 6));

    private final List<OreEntry> oreCache = new ArrayList<>();
    private ChunkPos lastChunk = null;
    private double savedGamma;

    public XRay() {
        super("XRay", "Highlights valuable ores through terrain", Category.RENDER);
    }

    @Override
    public void onEnable() {
        if (mc.options != null) {
            savedGamma = mc.options.getGamma().getValue();
            mc.options.getGamma().setValue(10.0);
        }
        oreCache.clear();
        lastChunk = null;
    }

    @Override
    public void onDisable() {
        if (mc.options != null) mc.options.getGamma().setValue(savedGamma);
        if (mc.player != null) mc.player.removeStatusEffect(StatusEffects.NIGHT_VISION);
        oreCache.clear();
        lastChunk = null;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        StatusEffectInstance nv = mc.player.getStatusEffect(StatusEffects.NIGHT_VISION);
        if (nv == null || nv.getDuration() < 100) {
            mc.player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.NIGHT_VISION, 9999, 0, false, false, false));
        }
        mc.options.getGamma().setValue(10.0);

        ChunkPos cur = new ChunkPos(mc.player.getBlockPos());
        if (lastChunk != null && cur.equals(lastChunk)) return;
        lastChunk = cur;
        scanChunks(cur);
    }

    private void scanChunks(ChunkPos center) {
        oreCache.clear();
        int cr = scanRange.get();
        for (int cx = center.x - cr; cx <= center.x + cr; cx++) {
            for (int cz = center.z - cr; cz <= center.z + cr; cz++) {
                WorldChunk chunk = mc.world.getChunkManager().getWorldChunk(cx, cz);
                if (chunk == null) continue;
                int minY = mc.world.getBottomY();
                int maxY = mc.world.getTopY();
                int bx = cx << 4, bz = cz << 4;
                for (int x = bx; x < bx + 16; x++) {
                    for (int z = bz; z < bz + 16; z++) {
                        for (int y = minY; y < maxY; y++) {
                            BlockPos pos = new BlockPos(x, y, z);
                            BlockState state = chunk.getBlockState(pos);
                            float[] col = getOreColor(state.getBlock());
                            if (col != null) {
                                oreCache.add(new OreEntry(pos.toImmutable(), col));
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onRender3D(EventRender3D event) {
        if (mc.world == null || mc.player == null) return;

        for (OreEntry entry : oreCache) {
            Box box = new Box(entry.pos);
            RenderUtil.drawESPBox(event.getMatrixStack(), box, entry.r, entry.g, entry.b, 1.0f, 1.5f);
            RenderUtil.drawFilledBox(event.getMatrixStack(), box, entry.r, entry.g, entry.b, 0.15f);
        }
    }

    private float[] getOreColor(Block block) {
        if (block == Blocks.DIAMOND_ORE || block == Blocks.DEEPSLATE_DIAMOND_ORE) {
            return diamond.isEnabled() ? new float[]{0.0f, 0.9f, 1.0f} : null;
        }
        if (block == Blocks.GOLD_ORE || block == Blocks.DEEPSLATE_GOLD_ORE || block == Blocks.NETHER_GOLD_ORE) {
            return gold.isEnabled() ? new float[]{1.0f, 0.85f, 0.0f} : null;
        }
        if (block == Blocks.IRON_ORE || block == Blocks.DEEPSLATE_IRON_ORE) {
            return iron.isEnabled() ? new float[]{0.9f, 0.6f, 0.4f} : null;
        }
        if (block == Blocks.EMERALD_ORE || block == Blocks.DEEPSLATE_EMERALD_ORE) {
            return emerald.isEnabled() ? new float[]{0.0f, 1.0f, 0.3f} : null;
        }
        if (block == Blocks.ANCIENT_DEBRIS) {
            return debris.isEnabled() ? new float[]{0.8f, 0.4f, 0.1f} : null;
        }
        if (block == Blocks.COAL_ORE || block == Blocks.DEEPSLATE_COAL_ORE) {
            return coal.isEnabled() ? new float[]{0.3f, 0.3f, 0.3f} : null;
        }
        if (block == Blocks.LAPIS_ORE || block == Blocks.DEEPSLATE_LAPIS_ORE) {
            return lapis.isEnabled() ? new float[]{0.2f, 0.3f, 1.0f} : null;
        }
        if (block == Blocks.REDSTONE_ORE || block == Blocks.DEEPSLATE_REDSTONE_ORE) {
            return redstone.isEnabled() ? new float[]{1.0f, 0.1f, 0.1f} : null;
        }
        if (block == Blocks.COPPER_ORE || block == Blocks.DEEPSLATE_COPPER_ORE) {
            return copper.isEnabled() ? new float[]{0.9f, 0.5f, 0.2f} : null;
        }
        if (block == Blocks.NETHER_QUARTZ_ORE) {
            return quartz.isEnabled() ? new float[]{1.0f, 0.95f, 0.95f} : null;
        }
        return null;
    }

    private static class OreEntry {
        final BlockPos pos;
        final float r, g, b;
        OreEntry(BlockPos pos, float[] col) {
            this.pos = pos;
            this.r = col[0]; this.g = col[1]; this.b = col[2];
        }
    }
}
