package com.ghostclient.module;

import com.ghostclient.GhostClient;
import com.ghostclient.module.modules.exploit.AntiCheat;
import com.ghostclient.module.modules.exploit.PacketFly;
import com.ghostclient.module.modules.exploit.Spoofer;
import com.ghostclient.module.modules.exploit.Timer;
import com.ghostclient.module.modules.player.*;
import com.ghostclient.module.modules.render.*;
import com.ghostclient.module.modules.world.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Manages all GhostClient modules - registration, lookup, and event routing.
 */
public class ModuleManager {

    private final List<Module> modules = new ArrayList<>();

    public void init() {
        // Player modules
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

        // Render modules
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

        // World modules
        register(new Nuker());
        register(new AutoFarm());
        register(new InstaBreak());
        register(new AutoMine());

        // Exploit modules
        register(new Timer());
        register(new Spoofer());
        register(new AntiCheat());
        register(new PacketFly());
    }

    private void register(Module module) {
        modules.add(module);
        GhostClient.getInstance().getEventBus().subscribe(module);
    }

    public void unregister(Module module) {
        modules.remove(module);
        GhostClient.getInstance().getEventBus().unsubscribe(module);
    }

    public List<Module> getModules() {
        return modules;
    }

    public List<Module> getModulesByCategory(Category category) {
        return modules.stream()
                .filter(m -> m.getCategory() == category)
                .collect(Collectors.toList());
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
