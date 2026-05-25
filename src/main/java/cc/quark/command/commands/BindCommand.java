package cc.quark.command.commands;

import cc.quark.command.Command;
import cc.quark.module.Module;
import cc.quark.module.ModuleManager;
import org.lwjgl.glfw.GLFW;

/**
 * Binds a keyboard key to a module for toggling.
 * Usage: .bind <module> <key|none>
 *
 * <p>Key names are matched against GLFW constants (e.g. "R" maps to GLFW_KEY_R).
 * Use "none" to clear a binding.
 */
public class BindCommand extends Command {

    private final ModuleManager moduleManager;

    public BindCommand(ModuleManager moduleManager) {
        super("bind", "Bind a key to a module.", "bind <module> <key|none>");
        this.moduleManager = moduleManager;
    }

    @Override
    public void execute(String[] args) {
        if (args.length < 2) {
            reply("§cUsage: .bind <module> <key|none>");
            return;
        }

        Module module = moduleManager.getModule(args[0]);
        if (module == null) {
            reply("§cModule not found: " + args[0]);
            return;
        }

        String keyName = args[1].toUpperCase();

        if (keyName.equals("NONE") || keyName.equals("0")) {
            module.setKeybind(-1);
            reply(module.getName() + " keybind cleared.");
            return;
        }

        int keyCode = resolveKey(keyName);
        if (keyCode == -1) {
            reply("§cUnknown key: " + args[1] + ". Use a single letter or GLFW key name.");
            return;
        }

        module.setKeybind(keyCode);
        reply(module.getName() + " bound to §b" + keyName + "§r.");
    }

    /**
     * Try to resolve a human-readable key name (e.g. "R", "F5", "HOME") to a GLFW key code.
     * Single uppercase letters are resolved directly via GLFW.
     */
    private int resolveKey(String name) {
        // Single alphabetic character
        if (name.length() == 1 && Character.isLetter(name.charAt(0))) {
            return GLFW.glfwGetKeyScancode(name.charAt(0)) != -1
                    ? (GLFW.GLFW_KEY_A + (name.charAt(0) - 'A'))
                    : -1;
        }

        // Common named keys
        return switch (name) {
            case "F1"  -> GLFW.GLFW_KEY_F1;
            case "F2"  -> GLFW.GLFW_KEY_F2;
            case "F3"  -> GLFW.GLFW_KEY_F3;
            case "F4"  -> GLFW.GLFW_KEY_F4;
            case "F5"  -> GLFW.GLFW_KEY_F5;
            case "F6"  -> GLFW.GLFW_KEY_F6;
            case "F7"  -> GLFW.GLFW_KEY_F7;
            case "F8"  -> GLFW.GLFW_KEY_F8;
            case "F9"  -> GLFW.GLFW_KEY_F9;
            case "F10" -> GLFW.GLFW_KEY_F10;
            case "F11" -> GLFW.GLFW_KEY_F11;
            case "F12" -> GLFW.GLFW_KEY_F12;
            case "HOME"   -> GLFW.GLFW_KEY_HOME;
            case "END"    -> GLFW.GLFW_KEY_END;
            case "INSERT" -> GLFW.GLFW_KEY_INSERT;
            case "DELETE" -> GLFW.GLFW_KEY_DELETE;
            case "RSHIFT" -> GLFW.GLFW_KEY_RIGHT_SHIFT;
            case "LSHIFT" -> GLFW.GLFW_KEY_LEFT_SHIFT;
            case "RCTRL"  -> GLFW.GLFW_KEY_RIGHT_CONTROL;
            case "LCTRL"  -> GLFW.GLFW_KEY_LEFT_CONTROL;
            case "RALT"   -> GLFW.GLFW_KEY_RIGHT_ALT;
            case "LALT"   -> GLFW.GLFW_KEY_LEFT_ALT;
            default -> -1;
        };
    }
}
