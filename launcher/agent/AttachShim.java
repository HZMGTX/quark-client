package cc.quark.agent;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

import java.util.List;

/**
 * Standalone shim that attaches a Java agent to a running JVM via the
 * JVM Attach API (com.sun.tools.attach).
 *
 * Usage: java -cp quark-agent.jar cc.quark.agent.AttachShim <pid> <agentJarPath>
 *
 * Exit codes:
 *   0 = success
 *   1 = usage error
 *   2 = attach failed
 */
public class AttachShim {

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: AttachShim <pid> <agentJar>");
            System.exit(1);
        }

        String pid      = args[0];
        String agentJar = args[1];
        String agentArgs = args.length > 2 ? args[2] : "";

        System.out.println("[AttachShim] Attaching to PID " + pid);
        System.out.println("[AttachShim] Agent: " + agentJar);

        VirtualMachine vm = null;
        try {
            vm = VirtualMachine.attach(pid);
            System.out.println("[AttachShim] Attached: " + vm.getSystemProperties().getProperty("java.vm.name", "?"));
            vm.loadAgent(agentJar, agentArgs.isEmpty() ? null : agentArgs);
            System.out.println("[AttachShim] [SUCCESS] Agent loaded successfully.");
        } catch (Exception e) {
            System.err.println("[AttachShim] Failed: " + e.getClass().getSimpleName() + ": " + e.getMessage());
            // Try to list available VMs for debugging
            try {
                List<VirtualMachineDescriptor> vms = VirtualMachine.list();
                System.err.println("[AttachShim] Available JVMs:");
                for (VirtualMachineDescriptor d : vms) {
                    System.err.println("  PID " + d.id() + " — " + d.displayName());
                }
            } catch (Exception ex) { /* ignore */ }
            System.exit(2);
        } finally {
            if (vm != null) try { vm.detach(); } catch (Exception ignored) {}
        }
    }
}
