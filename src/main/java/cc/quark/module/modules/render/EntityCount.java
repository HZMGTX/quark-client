package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;

/**
 * EntityCount — shows nearby entity counts in a HUD panel, categorized
 * as players, hostiles, animals, items, and vehicles.
 */
public class EntityCount extends Module {

    private final IntSetting xPos = register(new IntSetting(
            "X", "Horizontal position", 5, 0, 3000));

    private final IntSetting yPos = register(new IntSetting(
            "Y", "Vertical position", 120, 0, 3000));

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Count entities within this range (0 = all loaded)", 64.0, 0.0, 512.0));

    private final BoolSetting showPlayers = register(new BoolSetting(
            "Players", "Show nearby player count", true));

    private final BoolSetting showHostiles = register(new BoolSetting(
            "Hostiles", "Show nearby hostile mob count", true));

    private final BoolSetting showAnimals = register(new BoolSetting(
            "Animals", "Show nearby animal count", true));

    private final BoolSetting showItems = register(new BoolSetting(
            "Items", "Show nearby dropped item count", true));

    private final BoolSetting showVehicles = register(new BoolSetting(
            "Vehicles", "Show nearby vehicle count", false));

    private final BoolSetting showTotal = register(new BoolSetting(
            "Total", "Show total entity count", true));

    private final BoolSetting showBackground = register(new BoolSetting(
            "Background", "Draw background panel", true));

    // Cached counts (updated on tick to avoid per-frame world iteration)
    private int countPlayers, countHostiles, countAnimals, countItems, countVehicles, countTotal;

    public EntityCount() {
        super("EntityCount", "Shows nearby entity counts in HUD", Category.RENDER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.world == null || mc.player == null) return;

        int players = 0, hostiles = 0, animals = 0, items = 0, vehicles = 0, total = 0;
        double r = range.get();

        for (Entity e : mc.world.getEntities()) {
            if (e == mc.player) continue;
            if (r > 0 && mc.player.distanceTo(e) > r) continue;
            total++;

            if (e instanceof PlayerEntity)       players++;
            else if (e instanceof HostileEntity) hostiles++;
            else if (e instanceof AnimalEntity)  animals++;
            else if (e instanceof ItemEntity)    items++;
            else if (e instanceof BoatEntity || e instanceof AbstractMinecartEntity) vehicles++;
        }

        countPlayers  = players;
        countHostiles = hostiles;
        countAnimals  = animals;
        countItems    = items;
        countVehicles = vehicles;
        countTotal    = total;
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null) return;

        DrawContext ctx = event.getDrawContext();
        int x = xPos.get();
        int y = yPos.get();
        int lineH = mc.textRenderer.fontHeight + 2;

        java.util.List<String[]> rows = new java.util.ArrayList<>();
        if (showPlayers.isEnabled())  rows.add(new String[]{"§ePlayers:", "§f" + countPlayers});
        if (showHostiles.isEnabled()) rows.add(new String[]{"§cHostiles:", "§f" + countHostiles});
        if (showAnimals.isEnabled())  rows.add(new String[]{"§aAnimals:", "§f" + countAnimals});
        if (showItems.isEnabled())    rows.add(new String[]{"§6Items:", "§f" + countItems});
        if (showVehicles.isEnabled()) rows.add(new String[]{"§bVehicles:", "§f" + countVehicles});
        if (showTotal.isEnabled())    rows.add(new String[]{"§7Total:", "§f" + countTotal});

        if (rows.isEmpty()) return;

        // Compute label and value column widths
        int labelW = 0, valW = 0;
        for (String[] row : rows) {
            labelW = Math.max(labelW, mc.textRenderer.getWidth(net.minecraft.text.Text.of(row[0])));
            valW   = Math.max(valW,   mc.textRenderer.getWidth(net.minecraft.text.Text.of(row[1])));
        }
        int panelW = labelW + valW + 10;
        int panelH = rows.size() * lineH + 4;

        if (showBackground.isEnabled()) {
            ctx.fill(x - 3, y - 2, x + panelW, y + panelH, 0xAA111111);
        }

        for (String[] row : rows) {
            ctx.drawTextWithShadow(mc.textRenderer, net.minecraft.text.Text.of(row[0]), x, y, 0xFFFFFFFF);
            ctx.drawTextWithShadow(mc.textRenderer, net.minecraft.text.Text.of(row[1]),
                    x + labelW + 4, y, 0xFFFFFFFF);
            y += lineH;
        }
    }
}
