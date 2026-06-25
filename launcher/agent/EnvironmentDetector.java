package cc.quark.agent;

/**
 * Detects which Minecraft environment is running (mod loader, version, launcher)
 * so ClassResolver can pick the right class/method names.
 */
public class EnvironmentDetector {

    public enum Loader { FABRIC, FORGE, NEOFORGE, OPTIFINE, VANILLA, UNKNOWN }

    private static Loader loaderCache = null;
    private static String versionCache = null;

    // ── Loader Detection ─────────────────────────────────────────────────────

    public static Loader getLoader() {
        if (loaderCache != null) return loaderCache;

        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (classExists("net.fabricmc.loader.api.FabricLoader", cl)
         || classExists("net.fabricmc.loader.impl.FabricLoaderImpl", cl)
         || classExists("net.fabricmc.api.ModInitializer", cl)) {
            loaderCache = Loader.FABRIC;
        } else if (classExists("net.neoforged.neoforge.common.NeoForge", cl)
                || classExists("net.neoforged.fml.ModLoader", cl)) {
            loaderCache = Loader.NEOFORGE;
        } else if (classExists("net.minecraftforge.common.MinecraftForge", cl)
                || classExists("net.minecraftforge.fml.ModLoader", cl)
                || classExists("cpw.mods.fml.common.FMLCommonHandler", cl)) {
            loaderCache = Loader.FORGE;
        } else if (classExists("optifine.Installer", cl)
                || classExists("Config", cl)) {
            loaderCache = Loader.OPTIFINE;
        } else {
            loaderCache = Loader.VANILLA;
        }

        System.out.println("[Quark Env] Loader: " + loaderCache);
        return loaderCache;
    }

    // ── Minecraft Version Detection ───────────────────────────────────────────

    public static String getMinecraftVersion() {
        if (versionCache != null) return versionCache;

        // Try SharedConstants / GameVersion (Mojang/Fabric)
        String[] versionClasses = {
            "net.minecraft.SharedConstants",
            "net.minecraft.util.SharedConstants",
            "net.minecraft.class_156",           // intermediary
        };
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        for (String className : versionClasses) {
            try {
                Class<?> sc = Class.forName(className, false, cl);
                // Try getGameVersion().getName() (1.16+)
                try {
                    Object gv = sc.getDeclaredMethod("getGameVersion").invoke(null);
                    versionCache = (String) gv.getClass().getDeclaredMethod("getName").invoke(gv);
                    break;
                } catch (Exception ignored) {}
                // Try VERSION_STRING field or similar
                for (java.lang.reflect.Field f : sc.getDeclaredFields()) {
                    if (f.getType() == String.class
                        && java.lang.reflect.Modifier.isStatic(f.getModifiers())) {
                        f.setAccessible(true);
                        String val = (String) f.get(null);
                        if (val != null && val.matches("\\d+\\.\\d+.*")) {
                            versionCache = val;
                            break;
                        }
                    }
                }
                if (versionCache != null) break;
            } catch (Exception ignored) {}
        }

        if (versionCache == null) versionCache = "unknown";
        System.out.println("[Quark Env] Minecraft version: " + versionCache);
        return versionCache;
    }

    /** Returns the major+minor version as an int (e.g. 1211 for 1.21.1, 189 for 1.8.9) */
    public static int getVersionInt() {
        String v = getMinecraftVersion();
        try {
            String[] parts = v.split("\\.");
            int major = Integer.parseInt(parts[0]);
            int minor = Integer.parseInt(parts[1]);
            int patch = parts.length > 2 ? Integer.parseInt(parts[2].replaceAll("[^0-9]", "")) : 0;
            return major * 10000 + minor * 100 + patch;
        } catch (Exception e) {
            return 10000; // default to 1.0.0 if parse fails
        }
    }

    // ── Launcher Detection ────────────────────────────────────────────────────

    public enum LauncherType {
        OFFICIAL, TLAUNCHER, MULTIMC, PRISM, CURSEFORGE, MODRINTH, GDLAUNCHER,
        ATLAUNCHER, TECHNIC, FTB, LUNAR, BADLION, PVPLOUNGE, FEATHER, UNKNOWN
    }

    public static LauncherType getLauncher() {
        String userHome  = System.getProperty("user.home", "");
        String appData   = System.getenv("APPDATA") != null ? System.getenv("APPDATA") : "";
        String classpath = System.getProperty("java.class.path", "").toLowerCase();
        String cmdLine   = getCommandLine().toLowerCase();

        if (cmdLine.contains("tlauncher") || classpath.contains("tlauncher"))
            return LauncherType.TLAUNCHER;
        if (cmdLine.contains("prism") || classpath.contains("prism"))
            return LauncherType.PRISM;
        if (cmdLine.contains("multimc") || classpath.contains("multimc"))
            return LauncherType.MULTIMC;
        if (cmdLine.contains("curseclient") || classpath.contains("curseclient"))
            return LauncherType.CURSEFORGE;
        if (cmdLine.contains("modrinth") || classpath.contains("modrinth"))
            return LauncherType.MODRINTH;
        if (cmdLine.contains("gdlauncher") || classpath.contains("gdlauncher"))
            return LauncherType.GDLAUNCHER;
        if (cmdLine.contains("lunar") || classpath.contains("lunar"))
            return LauncherType.LUNAR;
        if (cmdLine.contains("badlion") || classpath.contains("badlion"))
            return LauncherType.BADLION;
        if (cmdLine.contains("pvplounge") || classpath.contains("pvplounge"))
            return LauncherType.PVPLOUNGE;
        if (cmdLine.contains("feather") || classpath.contains("feather"))
            return LauncherType.FEATHER;
        if (appData.contains("atlauncher") || classpath.contains("atlauncher"))
            return LauncherType.ATLAUNCHER;
        if (classpath.contains("technic"))
            return LauncherType.TECHNIC;
        if (classpath.contains("ftb") || classpath.contains("feedthebeast"))
            return LauncherType.FTB;

        return LauncherType.OFFICIAL;
    }

    private static String getCommandLine() {
        try {
            java.lang.management.RuntimeMXBean rb = java.lang.management.ManagementFactory.getRuntimeMXBean();
            return String.join(" ", rb.getInputArguments()) + " " + rb.getClassPath();
        } catch (Exception e) { return ""; }
    }

    private static boolean classExists(String name, ClassLoader cl) {
        try { Class.forName(name, false, cl); return true; }
        catch (ClassNotFoundException e) { return false; }
    }
}
