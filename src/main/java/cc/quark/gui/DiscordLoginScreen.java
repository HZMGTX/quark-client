package cc.quark.gui;

import cc.quark.Quark;
import cc.quark.util.ColorUtil;
import cc.quark.util.RenderUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class DiscordLoginScreen extends Screen {

    private float alpha = 0f;
    private int authState = 0; // 0 = default, 1 = authenticating, 2 = success, 3 = failed
    private long authStartTime = 0;

    // Discord Blurple color
    private static final int BLURPLE = 0xFF5865F2;

    public DiscordLoginScreen() {
        super(Text.literal("Quark Authentication"));
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        // We draw our own background
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        alpha = Math.min(1f, alpha + delta * 0.10f);

        int screenW = context.getScaledWindowWidth();
        int screenH = context.getScaledWindowHeight();

        // 1. Dark backdrop
        context.fill(0, 0, screenW, screenH, ColorUtil.withAlpha(0x0A0A0E, (int)(220 * alpha)));

        // 2. Center Modal
        int modalW = 320;
        int modalH = 200;
        int modalX = (screenW - modalW) / 2;
        int modalY = (screenH - modalH) / 2;

        // Modal shadow/outline
        context.fill(modalX - 1, modalY - 1, modalX + modalW + 1, modalY + modalH + 1, ColorUtil.withAlpha(BLURPLE & 0x00FFFFFF, (int)(150 * alpha)));
        context.fill(modalX, modalY, modalX + modalW, modalY + modalH, ColorUtil.withAlpha(0x111116, (int)(255 * alpha)));

        // 3. Title
        String title = "Authentication Required";
        int titleW = MinecraftClient.getInstance().textRenderer.getWidth(title);
        RenderUtil.drawCustomText(context, title, modalX + (modalW - titleW) / 2, modalY + 20, 0xFFFFFFFF);

        String subTitle = "To access Quark, you must link your Discord account.";
        int subTitleW = MinecraftClient.getInstance().textRenderer.getWidth(subTitle);
        RenderUtil.drawCustomText(context, subTitle, modalX + (modalW - subTitleW) / 2, modalY + 36, 0xFFAAAAAA);

        // 4. State Machine rendering
        if (authState == 0) {
            // Render Button
            int btnW = 200;
            int btnH = 36;
            int btnX = modalX + (modalW - btnW) / 2;
            int btnY = modalY + 100;

            boolean hovered = mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH;
            int btnColor = hovered ? 0xFF7289DA : BLURPLE;

            context.fill(btnX, btnY, btnX + btnW, btnY + btnH, ColorUtil.withAlpha(btnColor & 0x00FFFFFF, (int)(255 * alpha)));
            
            String btnText = "Login with Discord";
            int textW = MinecraftClient.getInstance().textRenderer.getWidth(btnText);
            RenderUtil.drawCustomText(context, btnText, btnX + (btnW - textW) / 2, btnY + (btnH - 8) / 2, 0xFFFFFFFF);
        } else if (authState == 1) {
            // Authenticating...
            long elapsed = System.currentTimeMillis() - authStartTime;
            String dots = "";
            if (elapsed % 1500 > 1000) dots = "...";
            else if (elapsed % 1500 > 500) dots = "..";
            else dots = ".";

            String authText = "Authenticating with Discord API" + dots;
            int authTextW = MinecraftClient.getInstance().textRenderer.getWidth(authText);
            RenderUtil.drawCustomText(context, authText, modalX + (modalW - authTextW) / 2, modalY + 110, BLURPLE);

            // Simulate finish
            if (elapsed > 2500) {
                authState = 2; // Success
                authStartTime = System.currentTimeMillis();
            }
        } else if (authState == 2) {
            // Success
            String successText = "Successfully Authenticated!";
            int successTextW = MinecraftClient.getInstance().textRenderer.getWidth(successText);
            RenderUtil.drawCustomText(context, successText, modalX + (modalW - successTextW) / 2, modalY + 110, 0xFF55FF55);

            String redirectText = "Redirecting to client...";
            int redirectTextW = MinecraftClient.getInstance().textRenderer.getWidth(redirectText);
            RenderUtil.drawCustomText(context, redirectText, modalX + (modalW - redirectTextW) / 2, modalY + 130, 0xFFAAAAAA);

            long elapsed = System.currentTimeMillis() - authStartTime;
            if (elapsed > 1000) {
                Quark.setAuthenticated(true);
                if (client != null) {
                    client.setScreen(new ClickGUI());
                }
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (authState == 0 && button == 0) {
            int screenW = width;
            int screenH = height;
            int modalW = 320;
            int modalH = 200;
            int modalX = (screenW - modalW) / 2;
            int modalY = (screenH - modalH) / 2;

            int btnW = 200;
            int btnH = 36;
            int btnX = modalX + (modalW - btnW) / 2;
            int btnY = modalY + 100;

            if (mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH) {
                // Clicked Login
                authState = 1;
                authStartTime = System.currentTimeMillis();
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false; // Force login!
    }
}
