package com.ghostclient.module.modules.render;

import com.ghostclient.event.EventHandler;
import com.ghostclient.event.events.EventRender3D;
import com.ghostclient.event.events.EventTick;
import com.ghostclient.module.Category;
import com.ghostclient.module.Module;
import com.ghostclient.setting.BoolSetting;
import com.ghostclient.util.RenderUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import java.util.ArrayList;
import java.util.List;

/**
 * XRay - renders valuable ore blocks through other terrain.
 *
 * Uses Night Vision + high gamma plus renders ESP outlines around each found ore
 * block within a scan radius so the player can see them through the terrain.
 *
 * Toggleable ore list:
 *   Diamond, Gold, Iron, Emerald, Ancient Debris, Coal, Lapis, Redstone,
 *   Copper, Nether Quartz, Nether Gold.
 */
public class XRay extends Module {

    private final BoolSetting diamond  = register(new BoolSetting("Diamond",  "Show diamond ore",        true));
    private final BoolSetting gold     = register(new BoolSetting("Gold",     "Show gold ore",            true));
    private final BoolSetting iron     = register(new BoolSetting("Iron",     "Show iron ore",            true));
    private final BoolSetting emerald  = register(new BoolSetting("Emerald",  "Show emerald ore",         true));
    private final BoolSetting debris   = register(new BoolSetting("Debris",   "Show ancient debris",      true));
    private final BoolSetting coal     = register(new BoolSetting("Coal",     "Show coal ore",            false));
    private final BoolSetting lapis    = register(new BoolSetting("Lapis",    "Show lapis ore",           true));
    private final BoolSetting redstone = register(new BoolSetting("Redstone", "Show redstone ore",        true));
    private final BoolSetting copper   = register(new BoolSetting("Copper",   "Show copper ore",          false));
    private final BoolSetting quartz   = register(new BoolSetting("Quartz",   "Show nether quartz ore",   false));

    private static final int SCAN_RADIUS = 16;

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
    }

    @Override
    public void onDisable() {
        if (mc.options != null) {
            mc.options.getGamma().setValue(savedGamma);
        }
        if (mc.player != null) {
            mc.player.removeStatusEffect(StatusEffects.NIGHT_VISION);
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        // Keep night vision active
        StatusEffectInstance nv = mc.player.getStatusEffect(StatusEffects.NIGHT_VISION);
        if (nv == null || nv.getDuration() < 100) {
            mc.player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.NIGHT_VISION, 9999, 0, false, false, false));
        }
        mc.options.getGamma().setValue(10.0);
    }

    @EventHandler
    public void onRender3D(EventRender3D event) {
        if (mc.world == null || mc.player == null) return;

        BlockPos center = mc.player.getBlockPos();

        for (int x = -SCAN_RADIUS; x <= SCAN_RADIUS; x++) {
            for (int y = -SCAN_RADIUS; y <= SCAN_RADIUS; y++) {
                for (int z = -SCAN_RADIUS; z <= SCAN_RADIUS; z++) {
                    BlockPos pos = center.add(x, y, z);
                    BlockState state = mc.world.getBlockState(pos);
                    Block block = state.getBlock();

                    float r, g, b;

                    if (block == Blocks.DIAMOND_ORE || block == Blocks.DEEPSLATE_DIAMOND_ORE) {
                        if (!diamond.isEnabled()) continue;
                        r = 0.0f; g = 0.9f; b = 1.0f;
                    } else if (block == Blocks.GOLD_ORE || block == Blocks.DEEPSLATE_GOLD_ORE
                            || block == Blocks.NETHER_GOLD_ORE) {
                        if (!gold.isEnabled()) continue;
                        r = 1.0f; g = 0.85f; b = 0.0f;
                    } else if (block == Blocks.IRON_ORE || block == Blocks.DEEPSLATE_IRON_ORE) {
                        if (!iron.isEnabled()) continue;
                        r = 0.9f; g = 0.6f; b = 0.4f;
                    } else if (block == Blocks.EMERALD_ORE || block == Blocks.DEEPSLATE_EMERALD_ORE) {
                        if (!emerald.isEnabled()) continue;
                        r = 0.0f; g = 1.0f; b = 0.3f;
                    } else if (block == Blocks.ANCIENT_DEBRIS) {
                        if (!debris.isEnabled()) continue;
                        r = 0.8f; g = 0.4f; b = 0.1f;
                    } else if (block == Blocks.COAL_ORE || block == Blocks.DEEPSLATE_COAL_ORE) {
                        if (!coal.isEnabled()) continue;
                        r = 0.3f; g = 0.3f; b = 0.3f;
                    } else if (block == Blocks.LAPIS_ORE || block == Blocks.DEEPSLATE_LAPIS_ORE) {
                        if (!lapis.isEnabled()) continue;
                        r = 0.2f; g = 0.3f; b = 1.0f;
                    } else if (block == Blocks.REDSTONE_ORE || block == Blocks.DEEPSLATE_REDSTONE_ORE) {
                        if (!redstone.isEnabled()) continue;
                        r = 1.0f; g = 0.1f; b = 0.1f;
                    } else if (block == Blocks.COPPER_ORE || block == Blocks.DEEPSLATE_COPPER_ORE) {
                        if (!copper.isEnabled()) continue;
                        r = 0.9f; g = 0.5f; b = 0.2f;
                    } else if (block == Blocks.NETHER_QUARTZ_ORE) {
                        if (!quartz.isEnabled()) continue;
                        r = 1.0f; g = 0.95f; b = 0.95f;
                    } else {
                        continue;
                    }

                    Box box = new Box(pos);
                    RenderUtil.drawESPBox(event.getMatrixStack(), box, r, g, b, 1.0f, 1.5f);
                    RenderUtil.drawFilledBox(event.getMatrixStack(), box, r, g, b, 0.15f);
                }
            }
        }
    }
}
