package cc.quark.ghost;

public class AntiCheatBypass {

    public static class NCP {
        public static double getSafeSpeed(boolean sprinting) { return sprinting ? 0.287 : 0.221; }
        public static boolean useAcceleration() { return true; }
        public static double getJumpModifier() { return 0.42; }
        public static double getSafeReach() { return 3.05; }
        public static int getMinAttackDelay() { return 8; }
        public static boolean requiresLineOfSight() { return false; }
    }

    public static class Grim {
        public static double getSafeSpeed() { return 0.2806; }
        public static boolean requiresOnGround() { return true; }
        public static int getMaxAirTicks() { return 12; }
        public static double getMaxReach() { return 3.0; }
        public static int getMinAttackDelay() { return 10; }
        public static double getMaxYDiff() { return 0.0625; }
        public static boolean strictPrediction() { return true; }
    }

    public static class Watchdog {
        public static double getSafeSpeed() { return 0.35; }
        public static double getSafeReach() { return 3.2; }
        public static int getAttackCooldown() { return 6; }
        public static boolean allowBHop() { return true; }
        public static int getMinAttackDelay() { return 6; }
        public static double getMaxCPS() { return 14.0; }

        /**
         * Apply pre-combat adjustments: throttle CPS to max 14/sec and ensure GCD is applied.
         * Call this before initiating an attack.
         */
        public static void applyPreCombat() {
            // Throttle CPS and ensure GCD is enforced — actual enforcement is in KillAura/AutoClicker
            // This method serves as a hook for external modules to call
        }

        /**
         * Apply post-combat adjustments: no-op for Watchdog.
         */
        public static void applyPostCombat() {
            // No post-combat adjustments needed for Watchdog
        }

        /**
         * Watchdog bans flight immediately — never allow flight.
         */
        public static boolean shouldAllowFlight() { return false; }

        /**
         * Maximum reach distance safe for Watchdog.
         */
        public static float getMaxReach() { return 3.2f; }

        /**
         * Maximum clicks per second safe for Watchdog.
         */
        public static int getCpsLimit() { return 14; }
    }

    public static class AAC {
        public static double getSafeSpeed() { return 0.26; }
        public static double getAirControl() { return 0.98; }
        public static boolean smoothMotion() { return true; }
        public static double getSafeReach() { return 3.1; }
        public static int getMinAttackDelay() { return 9; }
    }

    public static class Matrix {
        public static double getSafeSpeed() { return 0.34; }
        public static double getSafeReach() { return 3.15; }
        public static int getMaxViolations() { return 3; }
        public static int getMinAttackDelay() { return 7; }
        public static boolean requiresRotations() { return true; }

        /** Maximum reach distance safe for Matrix. */
        public static float getMaxReach() { return 3.15f; }

        /** Maximum clicks per second safe for Matrix. */
        public static int getCpsLimit() { return 13; }

        /**
         * Matrix detects extra packets sent outside normal movement cycles.
         * Return false to suppress any extra packet sending.
         */
        public static boolean shouldSendExtraPacket() { return false; }
    }

    public static class Spartan {
        public static double getSafeSpeed() { return 0.36; }
        public static boolean allowStrafe() { return true; }
        public static double getSafeReach() { return 3.2; }
        public static int getMinAttackDelay() { return 7; }
    }

    public static class Intave {
        public static double getSafeSpeed() { return 0.25; }
        public static double getSafeReach() { return 2.9; }
        public static long getMinAttackDelay() { return 100L; }
        public static boolean strictVelocityCheck() { return true; }
        public static double getMaxVelocityReduction() { return 0.95; }
        public static boolean strictMode() { return true; }
    }

    public static class Polar {
        public static double getSafeSpeed() { return 0.28; }
        public static double getSafeReach() { return 3.0; }
        public static long getMinAttackDelay() { return 85L; }
        public static int getMaxRotationDelta() { return 20; }
    }

    public static class Verus {
        public static double getSafeSpeed() { return 0.27; }
        public static double getSafeReach() { return 3.05; }
        public static long getMinAttackDelay() { return 72L; }
        public static int getMaxAirTicks() { return 10; }
        public static boolean requiresGroundReset() { return true; }
        public static double getMaxVelocityReduction() { return 0.85; }
    }

    public static class Karhu {
        public static double getSafeSpeed() { return 0.26; }
        public static double getSafeReach() { return 3.0; }
        public static long getMinAttackDelay() { return 80L; }
        public static boolean strictRotationCheck() { return true; }
        public static int getMaxRotationDelta() { return 18; }
        public static double getMaxVelocityReduction() { return 0.9; }
    }
}
