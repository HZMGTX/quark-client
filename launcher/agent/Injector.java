package cc.quark.agent;

import com.sun.tools.attach.VirtualMachine;
import java.io.File;

public class Injector {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: java cc.quark.agent.Injector <pid> <agent-jar-path>");
            System.exit(1);
        }
        
        String pid = args[0];
        String agentPath = new File(args[1]).getAbsolutePath();
        
        try {
            System.out.println("[Injector] Attaching to JVM " + pid + "...");
            VirtualMachine vm = VirtualMachine.attach(pid);
            
            System.out.println("[Injector] Loading Java Agent from " + agentPath + "...");
            vm.loadAgent(agentPath);
            
            System.out.println("[Injector] Detaching...");
            vm.detach();
            
            System.out.println("[Injector] SUCCESS!");
            System.exit(0);
        } catch (Exception e) {
            System.err.println("[Injector] FAILED to inject: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
