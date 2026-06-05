public class Dummy {
    public static void main(String[] args) throws Exception {
        System.out.println("Dummy running with PID: " + ProcessHandle.current().pid());
        while(true) { Thread.sleep(1000); }
    }
}
