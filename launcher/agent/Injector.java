package cc.quark.agent;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

import java.io.File;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Quark.cc Standalone Injector
 *
 * Finds a running Minecraft JVM and loads the Quark agent into it.
 *
 * Usage:
 *   java -jar quark-injector.jar               (auto-detect Minecraft)
 *   java -jar quark-injector.jar <pid>          (inject into specific PID)
 *
 * The injector must be run from the directory containing quark-agent.jar,
 * OR set QUARK_AGENT env-var to the full path of quark-agent.jar.
 */
public class Injector {

    public static void main(String[] args) throws Exception {
        printBanner();

        String agentPath = resolveAgentPath();
        System.out.println("[Injector] Agent JAR : " + agentPath);

        if (!new File(agentPath).exists()) {
            System.err.println("[Injector] ERROR: quark-agent.jar not found at: " + agentPath);
            System.err.println("[Injector] Build the agent first: ./gradlew buildAgent");
            System.exit(1);
        }

        String targetPid;

        if (args.length >= 1 && args[0].matches("\\d+")) {
            // PID provided on command line
            targetPid = args[0];
            System.out.println("[Injector] Target PID: " + targetPid + " (from args)");
        } else {
            // Auto-detect Minecraft
            targetPid = findMinecraftPid();
            if (targetPid == null) {
                System.err.println("[Injector] ERROR: No running Minecraft JVM found.");
                System.err.println("[Injector] Start Minecraft first, then run this injector.");
                System.err.println();
                System.err.println("[Injector] Running JVMs:");
                for (VirtualMachineDescriptor vmd : VirtualMachine.list()) {
                    System.err.println("  PID " + vmd.id() + "  " + vmd.displayName());
                }
                System.exit(1);
            }
            System.out.println("[Injector] Auto-detected Minecraft PID: " + targetPid);
        }

        inject(targetPid, agentPath);
    }

    private static void inject(String pid, String agentPath) throws Exception {
        VirtualMachine vm = null;
        try {
            System.out.println("[Injector] Attaching to JVM " + pid + "...");
            vm = VirtualMachine.attach(pid);
            System.out.println("[Injector] Attached. Loading agent...");
            vm.loadAgent(agentPath);
            System.out.println("[Injector] ✓ Quark.cc injected successfully!");
            System.out.println("[Injector] Press Right-Shift in Minecraft to open the GUI.");
        } catch (Exception e) {
            System.err.println("[Injector] FAILED: " + e.getMessage());
            if (e.getMessage() != null && e.getMessage().contains("permission")) {
                System.err.println("[Injector] Try running as Administrator / with sudo.");
            }
            e.printStackTrace();
            System.exit(1);
        } finally {
            if (vm != null) {
                vm.detach();
                System.out.println("[Injector] Detached from JVM.");
            }
        }
    }

    /**
     * Searches all running JVMs for one that looks like Minecraft.
     */
    private static String findMinecraftPid() throws Exception {
        List<VirtualMachineDescriptor> vms = VirtualMachine.list();
        // Priority 1: exact matches
        String[] priority1 = {"net.minecraft.client.main.Main", "GradleLauncher"};
        for (VirtualMachineDescriptor vmd : vms) {
            String name = vmd.displayName().toLowerCase();
            for (String kw : priority1) {
                if (name.contains(kw.toLowerCase())) return vmd.id();
            }
        }
        // Priority 2: common launcher patterns
        String[] priority2 = {"minecraft", "javaw", "lwjgl", "forge", "fabric"};
        for (VirtualMachineDescriptor vmd : vms) {
            String name = vmd.displayName().toLowerCase();
            for (String kw : priority2) {
                if (name.contains(kw)) return vmd.id();
            }
        }
        return null;
    }

    /**
     * Resolves the path to quark-agent.jar.
     * Checks: QUARK_AGENT env var → same dir as this JAR → ./quark-agent.jar
     */
    private static String resolveAgentPath() throws Exception {
        String envPath = System.getenv("QUARK_AGENT");
        if (envPath != null && !envPath.isBlank()) return envPath;

        // Same directory as the injector JAR
        String self = Injector.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        self = URLDecoder.decode(self, StandardCharsets.UTF_8);
        File selfDir = new File(self).getParentFile();
        File candidate = new File(selfDir, "quark-agent.jar");
        if (candidate.exists()) return candidate.getAbsolutePath();

        return new File("quark-agent.jar").getAbsolutePath();
    }

    private static void printBanner() {
        System.out.println();
        System.out.println(" ╔═══════════════════════════════════════════════╗");
        System.out.println(" ║         Quark.cc  ·  Ghost Client Injector    ║");
        System.out.println(" ║           Injection Mode  |  v1.0             ║");
        System.out.println(" ╚═══════════════════════════════════════════════╝");
        System.out.println();
    }
}
