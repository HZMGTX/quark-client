package cc.quark.agent;

import java.lang.instrument.Instrumentation;

public class QuarkAgent {
    public static void agentmain(String agentArgs, Instrumentation inst) {
        System.out.println("==================================================");
        System.out.println("[Quark Agent] ATTACHED SUCCESSFULLY!");
        System.out.println("[Quark Agent] Dynamic Ghost Client Initialized!");
        System.out.println("==================================================");
        
        // Removed Swing UI which crashes Minecraft's LWJGL thread
        new Thread(() -> {
            try {
                // Future C++ DLL style dynamic hooks go here
                Thread.sleep(100);
            } catch (Exception e) {}
        }).start();
    }
}
