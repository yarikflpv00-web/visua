package gg.donatil.visuals.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

/**
 * WASD-индикатор + ЛКМ/ПКМ.
 * Читает только локальное состояние клавиш — серверу ничего не шлём.
 */
public class KeystrokesHud {

    public boolean enabled = true;

    private final MinecraftClient mc = MinecraftClient.getInstance();

    // цвета
    private static final int COL_ACTIVE   = 0xCC9D6BFF;  // нажата
    private static final int COL_INACTIVE = 0x66000000;  // не нажата
    private static final int COL_BORDER   = 0x55FFFFFF;
    private static final int COL_TEXT_ON  = 0xFF0B0916;
    private static final int COL_TEXT_OFF = 0xFFC4A8FF;

    public void render(DrawContext ctx) {
        if (!enabled) return;

        long handle = mc.getWindow().getHandle();
        int sw = ctx.getScaledWindowWidth();
        int sh = ctx.getScaledWindowHeight();

        // Блок в правом нижнем углу
        int bw = 21;  // ширина одной кнопки
        int bh = 21;  // высота
        int gap = 2;  // зазор
        int blockW = bw * 3 + gap * 2;
        int startX = sw - blockW - 8;
        int startY = sh - (bh * 2 + gap) - 8;

        // WASD layout:
        //   [ W ]
        // [A][S][D]
        boolean w = isKey(handle, GLFW.GLFW_KEY_W);
        boolean a = isKey(handle, GLFW.GLFW_KEY_A);
        boolean s = isKey(handle, GLFW.GLFW_KEY_S);
        boolean d = isKey(handle, GLFW.GLFW_KEY_D);
        boolean lmb = GLFW.glfwGetMouseButton(handle, GLFW.GLFW_MOUSE_BUTTON_LEFT)
                == GLFW.GLFW_PRESS;
        boolean rmb = GLFW.glfwGetMouseButton(handle, GLFW.GLFW_MOUSE_BUTTON_RIGHT)
                == GLFW.GLFW_PRESS;

        // W по центру верхней строки
        drawKey(ctx, startX + bw + gap, startY, bw, bh, "W", w);

        // A S D
        drawKey(ctx, startX,            startY + bh + gap, bw, bh, "A", a);
        drawKey(ctx, startX + bw + gap, startY + bh + gap, bw, bh, "S", s);
        drawKey(ctx, startX + bw*2+gap*2, startY + bh + gap, bw, bh, "D", d);

        // LMB / RMB под WASD
        int mouseY = startY + (bh + gap) * 2;
        drawKey(ctx, startX,            mouseY, bw, bh, "LMB", lmb);
        drawKey(ctx, startX + bw*2+gap*2, mouseY, bw, bh, "RMB", rmb);
    }

    private boolean isKey(long handle, int key) {
        return InputUtil.isKeyPressed(handle, key);
    }

    private void drawKey(DrawContext ctx, int x, int y, int w, int h,
                         String label, boolean pressed) {
        // фон
        ctx.fill(x, y, x + w, y + h, pressed ? COL_ACTIVE : COL_INACTIVE);
        // рамка
        ctx.fill(x,   y,   x+w, y+1, COL_BORDER);
        ctx.fill(x,   y+h-1, x+w, y+h, COL_BORDER);
        ctx.fill(x,   y,   x+1, y+h, COL_BORDER);
        ctx.fill(x+w-1, y, x+w, y+h, COL_BORDER);
        // текст по центру
        int tw = MinecraftClient.getInstance().textRenderer.getWidth(label);
        int tx = x + (w - tw) / 2;
        int ty = y + (h - 9) / 2; // 9 = высота шрифта
        ctx.drawTextWithShadow(
                MinecraftClient.getInstance().textRenderer,
                label, tx, ty,
                pressed ? COL_TEXT_ON : COL_TEXT_OFF
        );
    }
}
