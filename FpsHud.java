package gg.donatil.visuals.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

/** Счётчик FPS в левом верхнем углу. */
public class FpsHud {

    public boolean enabled = true;

    private final MinecraftClient mc = MinecraftClient.getInstance();

    public void render(DrawContext ctx) {
        if (!enabled) return;

        String text = mc.getCurrentFps() + " FPS";
        int w = mc.textRenderer.getWidth(text);

        // фон
        ctx.fill(4, 4, 12 + w, 16, 0x88000000);
        // текст
        ctx.drawTextWithShadow(mc.textRenderer, text, 8, 6, 0xFF5CFFC0);
    }
}
