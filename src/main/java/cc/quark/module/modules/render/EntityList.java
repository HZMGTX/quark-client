package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;

public class EntityList extends Module {

    private final IntSetting  range       = register(new IntSetting("Range",   "Scan radius in blocks",               64, 20, 200));
    private final IntSetting  posX        = register(new IntSetting("X",       "HUD X position",                       5, 0, 1920));
    private final IntSetting  posY        = register(new IntSetting("Y",       "HUD Y position",                     100, 0, 1080));
    private final BoolSetting showPlayers = register(new BoolSetting("Players", "Count nearby players",               true));
    private final BoolSetting showMobs    = register(new BoolSetting("Mobs",    "Count nearby hostile mobs",           true));
    private final BoolSetting showAnimals = register(new BoolSetting("Animals", "Count nearby passive animals",        true));
    private final BoolSetting showItems   = register(new BoolSetting("Items",   "Count nearby item entities",          false));

    public EntityList() {
        super("EntityList", "HUD overlay listing nearby entities by type with counts", Category.RENDER);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.world == null || mc.player == null) return;

        int players = 0, mobs = 0, animals = 0, items = 0;
        double r = range.get();

        for (Entity e : mc.world.getEntities()) {
            if (e == mc.player) continue;
            if (mc.player.distanceTo(e) > r) continue;
            if (e instanceof PlayerEntity)   players++;
            else if (e instanceof AnimalEntity) animals++;
            else if (e instanceof MobEntity)  mobs++;
            else if (e instanceof ItemEntity) items++;
        }

        DrawContext ctx = event.getDrawContext();
        int x = posX.get(), y = posY.get();
        int lineH = mc.textRenderer.fontHeight + 2;

        int accent = cc.quark.gui.ClickGUI.getAccentColor();

        ctx.fill(x - 2, y - 2, x + 90, y + lineH * countLines() + 2, 0xAA111111);
        ctx.fill(x - 2, y - 2, x, y + lineH * countLines() + 2, accent);

        int dy = y;
        if (showPlayers.isEnabled()) {
            ctx.drawTextWithShadow(mc.textRenderer, "Players: " + players, x + 3, dy, 0xFFFF5555);
            dy += lineH;
        }
        if (showMobs.isEnabled()) {
            ctx.drawTextWithShadow(mc.textRenderer, "Mobs: " + mobs, x + 3, dy, 0xFFFF8844);
            dy += lineH;
        }
        if (showAnimals.isEnabled()) {
            ctx.drawTextWithShadow(mc.textRenderer, "Animals: " + animals, x + 3, dy, 0xFF55FF55);
            dy += lineH;
        }
        if (showItems.isEnabled()) {
            ctx.drawTextWithShadow(mc.textRenderer, "Items: " + items, x + 3, dy, 0xFFFFFF55);
        }
    }

    private int countLines() {
        int c = 0;
        if (showPlayers.isEnabled()) c++;
        if (showMobs.isEnabled())    c++;
        if (showAnimals.isEnabled()) c++;
        if (showItems.isEnabled())   c++;
        return Math.max(1, c);
    }
}
