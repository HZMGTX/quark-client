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
    public void onTick(cc.quark.event.events.EventTick event) {}

    private void saveMapImage(MapState mapState, int mapId) {
        // Implementation commented out for 1.21.1
    }

    /** Manually trigger a save of the current slot's map. */
    public void saveNow() {
        // Implementation commented out for 1.21.1
    }
}
