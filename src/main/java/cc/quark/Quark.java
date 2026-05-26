package cc.quark;

import cc.quark.command.CommandManager;
import cc.quark.config.ConfigManager;
import cc.quark.event.EventBus;
import cc.quark.event.EventHandler;
import cc.quark.event.events.EventKey;
import cc.quark.friend.FriendManager;
import cc.quark.module.ModuleManager;
import cc.quark.waypoint.WaypointManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main entry point for Quark.
 * Implements both {@link ModInitializer} (server-safe) and
 * {@link ClientModInitializer} (client-only bootstrap).
 */
public class Quark implements ModInitializer, ClientModInitializer {

    public static final String MOD_ID   = "quark";
    public static final String MOD_NAME = "Quark.cc";
    public static final String VERSION  = "1.0.0";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    /** Singleton instance, set in {@link #onInitializeClient()}. */
    private static Quark instance;

    // Core subsystems
    private EventBus        eventBus;
    private ModuleManager   moduleManager;
    private ConfigManager   configManager;
    private FriendManager   friendManager;
    private WaypointManager waypointManager;
    private CommandManager  commandManager;

    /** GLFW keybind to open / close the ClickGUI (default: Right Shift). */
    private KeyBinding guiKeyBinding;

    // -------------------------------------------------------------------------
    // ModInitializer â€” runs on both sides (safe init only)
    // -------------------------------------------------------------------------

    @Override
    public void onInitialize() {
        LOGGER.info("{} {} loading (common init).", MOD_NAME, VERSION);
        // No server-side registration needed for a client-only mod.
    }

    // -------------------------------------------------------------------------
    // ClientModInitializer â€” client side only
    // -------------------------------------------------------------------------

    @Override
    public void onInitializeClient() {
        LOGGER.info("{} {} initialising client subsystems.", MOD_NAME, VERSION);

        instance = this;

        // Boot order matters: EventBus first, then managers that subscribe.
        eventBus        = new EventBus();
        friendManager   = new FriendManager();
        moduleManager   = new ModuleManager();
        configManager   = new ConfigManager(moduleManager);
        waypointManager = new WaypointManager();
        try {
            waypointManager.load();
        } catch (Exception ex) {
            LOGGER.warn("Could not load waypoints (first run?): {}", ex.getMessage());
        }
        commandManager = new CommandManager(moduleManager, configManager, friendManager, waypointManager);

        // Init modules (registers them with the event bus).
        moduleManager.init();

        // Try to restore saved configuration.
        try {
            configManager.load();
        } catch (Exception ex) {
            LOGGER.warn("Could not load config (first run?): {}", ex.getMessage());
        }

        // Subscribe self to receive EventKey from the mixin.
        eventBus.subscribe(this);

        // Hook into Fabric's client tick to drive ModuleManager & keybinds.
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.world == null) return;
            moduleManager.onTick();
        });

        LOGGER.info("{} ready.", MOD_NAME);
    }

    // -------------------------------------------------------------------------
    // EventHandler: keyboard events posted by MixinMinecraft
    // -------------------------------------------------------------------------

    @EventHandler
    public void onKey(EventKey event) {
        moduleManager.onKey(event.getKeyCode());
    }

    // -------------------------------------------------------------------------
    // GUI
    // -------------------------------------------------------------------------

    private void openGui(MinecraftClient mc) {
        if (mc.currentScreen == null) {
            mc.setScreen(new cc.quark.gui.ClickGUI());
        } else {
            mc.setScreen(null);
        }
    }

    // -------------------------------------------------------------------------
    // Accessors
    // -------------------------------------------------------------------------

    public static Quark getInstance() {
        return instance;
    }

    public EventBus getEventBus() {
        return eventBus;
    }

    public ModuleManager getModuleManager() {
        return moduleManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public FriendManager getFriendManager() {
        return friendManager;
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }

    public WaypointManager getWaypointManager() {
        return waypointManager;
    }
}
