package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.ChatUtil;
import cc.quark.util.TimerUtil;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.map.MapState;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * MapSave - Saves map item data as a PNG image in .minecraft/quark/maps/.
 * When autoSave is on, saves every time a map is detected in the watched slot.
 */
public class MapSave extends Module {

    private final BoolSetting autoSave = register(new BoolSetting(
            "AutoSave", "Automatically save when a map is held", true));
    private final IntSetting slot = register(new IntSetting(
            "Slot", "Hotbar slot to watch (0-8)", 0, 0, 8));

    private final TimerUtil timer = new TimerUtil();
    private int lastMapId = -1;

    public MapSave() {
        super("MapSave", "Saves map item data to file", Category.WORLD);
    }

    @Override
    public void onEnable() {
        lastMapId = -1;
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (!autoSave.isEnabled()) return;
        if (mc.player == null || mc.world == null) return;
        if (!timer.hasReached(2000)) return;

        ItemStack stack = mc.player.getInventory().getStack(slot.get());
        if (!(stack.getItem() instanceof FilledMapItem)) return;

        Integer mapId = FilledMapItem.getMapId(stack);
        if (mapId == null || mapId == lastMapId) return;

        MapState mapState = FilledMapItem.getMapState(mapId, mc.world);
        if (mapState == null) return;

        lastMapId = mapId;
        saveMapImage(mapState, mapId);
        timer.reset();
    }

    private void saveMapImage(MapState mapState, int mapId) {
        try {
            File dir = new File(mc.runDirectory, "quark/maps");
            dir.mkdirs();

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            File file = new File(dir, "map_" + mapId + "_" + timestamp + ".png");

            BufferedImage image = new BufferedImage(128, 128, BufferedImage.TYPE_INT_RGB);
            for (int y = 0; y < 128; y++) {
                for (int x = 0; x < 128; x++) {
                    int colorIndex = mapState.colors[x + y * 128] & 0xFF;
                    int[] rgb = net.minecraft.util.math.ColorHelper.Argb.unpack(
                            net.minecraft.block.MapColor.getRenderColor(colorIndex));
                    image.setRGB(x, y, new Color(rgb[1], rgb[2], rgb[3]).getRGB());
                }
            }

            ImageIO.write(image, "png", file);
            ChatUtil.success("Map saved: " + file.getName());
        } catch (IOException | Exception e) {
            ChatUtil.error("MapSave failed: " + e.getMessage());
        }
    }

    /** Manually trigger a save of the current slot's map. */
    public void saveNow() {
        if (mc.player == null || mc.world == null) return;
        ItemStack stack = mc.player.getInventory().getStack(slot.get());
        if (!(stack.getItem() instanceof FilledMapItem)) {
            ChatUtil.error("No map in slot " + slot.get());
            return;
        }
        Integer mapId = FilledMapItem.getMapId(stack);
        if (mapId == null) return;
        MapState mapState = FilledMapItem.getMapState(mapId, mc.world);
        if (mapState == null) return;
        saveMapImage(mapState, mapId);
    }
}
