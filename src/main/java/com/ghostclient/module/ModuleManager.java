package com.ghostclient.module;

import com.ghostclient.GhostClient;
import com.ghostclient.module.modules.combat.*;
import com.ghostclient.module.modules.exploit.*;
import com.ghostclient.module.modules.movement.*;
import com.ghostclient.module.modules.player.*;
import com.ghostclient.module.modules.render.*;
import com.ghostclient.module.modules.world.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Central registry for all GhostClient modules.
 *
 * <p>Modules are instantiated in {@link #init()}, subscribed to the EventBus,
 * and can later be looked up by class or name.
 */
public class ModuleManager {

    private final List<Module> modules = new ArrayList<>();

    // -------------------------------------------------------------------------
    // Initialisation
    // -------------------------------------------------------------------------

    /**
     * Instantiate and register all modules. Call once during client init.
     */
    public void init() {

        // -------- COMBAT --------
        register(new KillAura());
        register(new AimAssist());
        register(new Criticals());
        register(new Velocity());
        register(new Reach());
        register(new AutoTotem());
        register(new AutoArmor());
        register(new AntiBot());
        register(new BowAimbot());
        register(new AutoCrystal());

        // -------- MOVEMENT --------
        register(new Sprint());
        register(new Speed());
        register(new Fly());
        register(new NoFall());
        register(new Step());
        register(new SafeWalk());
        register(new NoSlowdown());
        register(new Parkour());
        register(new Spider());
        register(new HighJump());
        register(new LongJump());
        register(new FastLadder());
        register(new Glide());
        register(new IceSpeed());
        register(new AirJump());
        register(new ElytraFly());
        register(new Phase());
        register(new FastFall());
        register(new AntiVoid());
        register(new Jesus());
        register(new Strafe());

        // -------- PLAYER --------
        register(new AntiAFK());
        register(new AutoEat());
        register(new FastEat());
        register(new NoHunger());
        register(new Freecam());
        register(new FastBreak());
        register(new FastPlace());
        register(new Scaffold());
        register(new Blink());
        register(new ChestStealer());
        register(new AutoFish());
        register(new AutoRespawn());
        register(new NoRotate());
        register(new InventoryManager());
        register(new ChestAura());
        register(new PacketLogger());
        register(new AutoTool());
        register(new MultiTask());

        // -------- RENDER --------
        register(new ESP());
        register(new Tracers());
        register(new Fullbright());
        register(new Nametags());
        register(new ChestESP());
        register(new XRay());
        register(new HoleESP());
        register(new HUD());
        register(new Breadcrumbs());
        register(new NoHurtCam());
        register(new ArmorHUD());
        register(new Zoom());
        register(new Crosshair());
        register(new NoFog());
        register(new FreeLook());
        register(new PotionHUD());
        register(new ItemESP());
        register(new TimeChanger());
        register(new StorageESP());

        // -------- WORLD --------
        register(new Nuker());
        register(new AutoFarm());
        register(new InstaBreak());
        register(new AutoMine());
        register(new MiddleClick());
        register(new Tunneler());

        // -------- EXPLOIT --------
        register(new Timer());
        register(new Spoofer());
        register(new AntiCheat());
        register(new PacketFly());
        register(new BoatFly());
        register(new LagSwitch());
        register(new NameProtect());
        register(new AutoWalk());
        register(new Disabler());
        register(new PortalGod());
    }

    // -------------------------------------------------------------------------
    // Registration helpers
    // -------------------------------------------------------------------------

    private void register(Module module) {
        modules.add(module);
        // Subscribe to event bus so @EventHandler methods fire automatically.
        GhostClient.getInstance().getEventBus().subscribe(module);
    }

    public void unregister(Module module) {
        modules.remove(module);
        GhostClient.getInstance().getEventBus().unsubscribe(module);
    }

    // -------------------------------------------------------------------------
    // Tick & keybind handling
    // -------------------------------------------------------------------------

    /**
     * Called every client tick by the Fabric tick event hook in GhostClient.
     * Forwards the tick to every currently-enabled module that overrides onTick.
     */
    public void onTick() {
        for (Module module : modules) {
            if (module.isEnabled()) {
                try {
                    module.onTick();
                } catch (Exception e) {
                    GhostClient.LOGGER.error("Exception in module tick [{}]: {}",
                            module.getName(), e.getMessage(), e);
                }
            }
        }
    }

    /**
     * Called when a keyboard key is pressed (from MixinMinecraft via EventKey).
     * Toggles any module whose keybind matches.
     *
     * @param keyCode GLFW key code
     */
    public void onKey(int keyCode) {
        if (keyCode <= 0) return;
        for (Module module : modules) {
            if (module.getKeybind() == keyCode) {
                module.toggle();
            }
        }
    }

    // -------------------------------------------------------------------------
    // Lookups
    // -------------------------------------------------------------------------

    public List<Module> getModules() {
        return modules;
    }

    public List<Module> getModulesForCategory(Category category) {
        return modules.stream()
                .filter(m -> m.getCategory() == category)
                .collect(Collectors.toList());
    }

    public List<Module> getModulesByCategory(Category category) {
        return getModulesForCategory(category);
    }

    public Module getModule(String name) {
        return modules.stream()
                .filter(m -> m.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    public <T extends Module> T getModule(Class<T> clazz) {
        for (Module m : modules) {
            if (clazz.isInstance(m)) {
                return clazz.cast(m);
            }
        }
        return null;
    }

    public List<Module> getEnabledModules() {
        return modules.stream()
                .filter(Module::isEnabled)
                .sorted(Comparator.comparingInt(m -> m.getName().length()))
                .collect(Collectors.toList());
    }

    public boolean isEnabled(Class<? extends Module> clazz) {
        Module m = getModule(clazz);
        return m != null && m.isEnabled();
    }
}
