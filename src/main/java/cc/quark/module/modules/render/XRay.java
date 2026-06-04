package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender3D;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.setting.ModeSetting;
import cc.quark.util.RenderUtil;
import net.minecraft.block.*;
import net.minecraft.block.entity.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class XRay extends Module {

    // ── Static state shared with mixins ──────────────────────────────────────
    private static volatile boolean xrayActive = false;
    private static volatile boolean pendingReload = false;
    private static final Set<Block> WHITELIST = ConcurrentHashMap.newKeySet();

    // ── Settings ─────────────────────────────────────────────────────────────
    private final ModeSetting  renderMode = register(new ModeSetting("Mode", "Rendering approach",
            "Both", "Both", "True XRay", "ESP Only"));

    private final BoolSetting  diamond   = register(new BoolSetting("Diamond",  "Diamond ore",         true));
    private final BoolSetting  gold      = register(new BoolSetting("Gold",     "Gold ore",             true));
    private final BoolSetting  iron      = register(new BoolSetting("Iron",     "Iron ore",             true));
    private final BoolSetting  emerald   = register(new BoolSetting("Emerald",  "Emerald ore",          true));
    private final BoolSetting  debris    = register(new BoolSetting("Debris",   "Ancient debris",       true));
    private final BoolSetting  coal      = register(new BoolSetting("Coal",     "Coal ore",             false));
    private final BoolSetting  lapis     = register(new BoolSetting("Lapis",    "Lapis ore",            true));
    private final BoolSetting  redstone  = register(new BoolSetting("Redstone", "Redstone ore",         true));
    private final BoolSetting  copper    = register(new BoolSetting("Copper",   "Copper ore",           false));
    private final BoolSetting  quartz    = register(new BoolSetting("Quartz",   "Nether quartz ore",    false));
    private final BoolSetting  chests    = register(new BoolSetting("Chests",   "Chests / barrels",     false));
    private final BoolSetting  spawners  = register(new BoolSetting("Spawners", "Monster spawners",     false));
    private final IntSetting   scanRange = register(new IntSetting("Range",     "Scan radius in chunks", 3, 1, 8));

    private final List<OreEntry> oreCache = new ArrayList<>();
    private ChunkPos lastChunk  = null;
    private double   savedGamma = 1.0;

    public XRay() {
        super("XRay", "True XRay — hides terrain to reveal ores + ESP box overlay", Category.RENDER);
    }

    // ── Mixin API ─────────────────────────────────────────────────────────────

    public static boolean isXrayActive() { return xrayActive; }

    public static boolean isWhitelisted(BlockState state) {
        if (state == null) return false;
        Block b = state.getBlock();
        if (b == Blocks.AIR || b == Blocks.CAVE_AIR || b == Blocks.VOID_AIR) return true;
        return WHITELIST.contains(b);
    }

    public static void onChunkReload() { pendingReload = false; }

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    @Override
    public void onEnable() {
        if (mc.options != null) {
            savedGamma = mc.options.getGamma().getValue();
            mc.options.getGamma().setValue(15.0);
        }
        oreCache.clear();
        lastChunk = null;
        rebuildWhitelist();
        xrayActive = !renderMode.is("ESP Only");
        if (xrayActive) triggerChunkReload();
    }

    @Override
    public void onDisable() {
        xrayActive = false;
        WHITELIST.clear();
        if (mc.options != null) mc.options.getGamma().setValue(savedGamma);
        if (mc.player != null) mc.player.removeStatusEffect(StatusEffects.NIGHT_VISION);
        oreCache.clear();
        triggerChunkReload();
    }

    // ── Events ────────────────────────────────────────────────────────────────

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        // Keep Night Vision applied so underground areas are visible
        StatusEffectInstance nv = mc.player.getStatusEffect(StatusEffects.NIGHT_VISION);
        if (nv == null || nv.getDuration() < 100) {
            mc.player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.NIGHT_VISION, 9999, 0, false, false, false));
        }
        mc.options.getGamma().setValue(15.0);

        // Rebuild whitelist if settings changed, then reload chunks
        rebuildWhitelist();
        boolean wantTrueXray = !renderMode.is("ESP Only");
        if (xrayActive != wantTrueXray) {
            xrayActive = wantTrueXray;
            triggerChunkReload();
        }

        // Rescan on chunk boundary
        ChunkPos cur = new ChunkPos(mc.player.getBlockPos());
        if (lastChunk == null || !cur.equals(lastChunk)) {
            lastChunk = cur;
            scanChunks(cur);
        }
    }

    @EventHandler
    public void onRender3D(EventRender3D event) {
        if (mc.world == null || mc.player == null) return;
        if (renderMode.is("True XRay")) return; // only show boxes in ESP Only / Both

        for (OreEntry e : oreCache) {
            Box box = new Box(e.pos);
            RenderUtil.drawESPBox(event.getMatrixStack(), box, e.r, e.g, e.b, 1.0f, 1.5f);
            RenderUtil.drawFilledBox(event.getMatrixStack(), box, e.r, e.g, e.b, 0.15f);
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void rebuildWhitelist() {
        WHITELIST.clear();
        // Transparent/structural blocks always visible in true-xray mode
        addIf(true, Blocks.AIR, Blocks.CAVE_AIR, Blocks.VOID_AIR,
                Blocks.WATER, Blocks.LAVA,
                Blocks.GLASS, Blocks.GLASS_PANE, Blocks.TINTED_GLASS,
                Blocks.ICE, Blocks.FROSTED_ICE, Blocks.BLUE_ICE, Blocks.PACKED_ICE,
                Blocks.TORCH, Blocks.WALL_TORCH, Blocks.REDSTONE_TORCH, Blocks.SOUL_TORCH,
                Blocks.LADDER, Blocks.VINE, Blocks.IRON_BARS,
                Blocks.CHEST, Blocks.TRAPPED_CHEST, Blocks.ENDER_CHEST,
                Blocks.ENCHANTING_TABLE, Blocks.CRAFTING_TABLE, Blocks.FURNACE);
        addIf(diamond.isEnabled(), Blocks.DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE);
        addIf(gold.isEnabled(), Blocks.GOLD_ORE, Blocks.DEEPSLATE_GOLD_ORE, Blocks.NETHER_GOLD_ORE);
        addIf(iron.isEnabled(), Blocks.IRON_ORE, Blocks.DEEPSLATE_IRON_ORE);
        addIf(emerald.isEnabled(), Blocks.EMERALD_ORE, Blocks.DEEPSLATE_EMERALD_ORE);
        addIf(debris.isEnabled(), Blocks.ANCIENT_DEBRIS);
        addIf(coal.isEnabled(), Blocks.COAL_ORE, Blocks.DEEPSLATE_COAL_ORE);
        addIf(lapis.isEnabled(), Blocks.LAPIS_ORE, Blocks.DEEPSLATE_LAPIS_ORE);
        addIf(redstone.isEnabled(), Blocks.REDSTONE_ORE, Blocks.DEEPSLATE_REDSTONE_ORE);
        addIf(copper.isEnabled(), Blocks.COPPER_ORE, Blocks.DEEPSLATE_COPPER_ORE);
        addIf(quartz.isEnabled(), Blocks.NETHER_QUARTZ_ORE);
        addIf(chests.isEnabled(), Blocks.CHEST, Blocks.TRAPPED_CHEST, Blocks.BARREL, Blocks.SHULKER_BOX);
        addIf(spawners.isEnabled(), Blocks.SPAWNER, Blocks.TRIAL_SPAWNER);
    }

    private void addIf(boolean cond, Block... blocks) {
        if (!cond) return;
        for (Block b : blocks) WHITELIST.add(b);
    }

    private void triggerChunkReload() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.worldRenderer != null && !pendingReload) {
            pendingReload = true;
            mc.worldRenderer.reload();
        }
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
                            if (col != null) oreCache.add(new OreEntry(pos.toImmutable(), col));
                        }
                    }
                }
                // Also scan block entities (chests, spawners) in this chunk
                if (chests.isEnabled() || spawners.isEnabled()) {
                    for (BlockEntity be : chunk.getBlockEntities().values()) {
                        float[] col = getBeColor(be);
                        if (col != null) oreCache.add(new OreEntry(be.getPos().toImmutable(), col));
                    }
                }
            }
        }
    }

    private float[] getOreColor(Block b) {
        if (diamond.isEnabled() && (b == Blocks.DIAMOND_ORE || b == Blocks.DEEPSLATE_DIAMOND_ORE))
            return new float[]{0.0f, 0.9f, 1.0f};
        if (gold.isEnabled() && (b == Blocks.GOLD_ORE || b == Blocks.DEEPSLATE_GOLD_ORE || b == Blocks.NETHER_GOLD_ORE))
            return new float[]{1.0f, 0.85f, 0.0f};
        if (iron.isEnabled() && (b == Blocks.IRON_ORE || b == Blocks.DEEPSLATE_IRON_ORE))
            return new float[]{0.9f, 0.6f, 0.4f};
        if (emerald.isEnabled() && (b == Blocks.EMERALD_ORE || b == Blocks.DEEPSLATE_EMERALD_ORE))
            return new float[]{0.0f, 1.0f, 0.3f};
        if (debris.isEnabled() && b == Blocks.ANCIENT_DEBRIS)
            return new float[]{0.8f, 0.4f, 0.1f};
        if (coal.isEnabled() && (b == Blocks.COAL_ORE || b == Blocks.DEEPSLATE_COAL_ORE))
            return new float[]{0.35f, 0.35f, 0.35f};
        if (lapis.isEnabled() && (b == Blocks.LAPIS_ORE || b == Blocks.DEEPSLATE_LAPIS_ORE))
            return new float[]{0.2f, 0.3f, 1.0f};
        if (redstone.isEnabled() && (b == Blocks.REDSTONE_ORE || b == Blocks.DEEPSLATE_REDSTONE_ORE))
            return new float[]{1.0f, 0.1f, 0.1f};
        if (copper.isEnabled() && (b == Blocks.COPPER_ORE || b == Blocks.DEEPSLATE_COPPER_ORE))
            return new float[]{0.9f, 0.5f, 0.2f};
        if (quartz.isEnabled() && b == Blocks.NETHER_QUARTZ_ORE)
            return new float[]{1.0f, 0.95f, 0.95f};
        return null;
    }

    private float[] getBeColor(BlockEntity be) {
        if (chests.isEnabled() && (be instanceof ChestBlockEntity || be instanceof BarrelBlockEntity))
            return new float[]{1.0f, 0.6f, 0.0f};
        if (chests.isEnabled() && be instanceof ShulkerBoxBlockEntity)
            return new float[]{0.7f, 0.2f, 1.0f};
        if (spawners.isEnabled() && be instanceof MobSpawnerBlockEntity)
            return new float[]{0.0f, 0.8f, 0.0f};
        return null;
    }

    @Override
    public String getSuffix() { return renderMode.get(); }

    private static class OreEntry {
        final BlockPos pos;
        final float r, g, b;
        OreEntry(BlockPos pos, float[] col) {
            this.pos = pos; r = col[0]; g = col[1]; b = col[2];
        }
    }
}
