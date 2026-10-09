package gg.donatil.visuals.ui;

import gg.donatil.visuals.DonatilVisuals;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

/**
 * Меню модулей.
 * Открывается: RIGHT SHIFT
 * Закрывается: RIGHT SHIFT ещё раз ИЛИ ESC (стандартный close())
 *
 * Клик ЛКМ по строке — включает / выключает модуль.
 * shouldPause() = false — игра не встаёт на паузу.
 */
public class ClickGuiScreen extends Screen {

    private static final int PANEL_W  = 280;
    private static final int TITLE_H  = 48;
    private static final int ROW_H    = 26;
    private static final int FOOTER_H = 12;

    // Модули в виде простых структур
    private record ModEntry(String name, String hint, Getter get, Setter set) {}
    @FunctionalInterface interface Getter { boolean get(); }
    @FunctionalInterface interface Setter { void set(boolean v); }

    private final ModEntry[] entries = {
        new ModEntry("FPS HUD",     "Счётчик кадров в секунду",
                () -> DonatilVisuals.FPS_HUD.enabled,
                v -> DonatilVisuals.FPS_HUD.enabled = v),
        new ModEntry("Keystrokes",  "WASD + ЛКМ/ПКМ индикатор",
                () -> DonatilVisuals.KEYS_HUD.enabled,
                v -> DonatilVisuals.KEYS_HUD.enabled = v),
        new ModEntry("Buff Timer",  "Таймер баффов и дебаффов",
                () -> DonatilVisuals.BUFF_HUD.enabled,
                v -> DonatilVisuals.BUFF_HUD.enabled = v),
        new ModEntry("Fullbright",  "Высокая гамма (видно в темноте)",
                () -> DonatilVisuals.fullbrightEnabled,
                v -> DonatilVisuals.fullbrightEnabled = v),
    };

    public ClickGuiScreen() {
        super(Text.literal("Donatil Visuals"));
    }

    private int panelH() {
        return TITLE_H + ROW_H * entries.length + FOOTER_H + 8;
    }

    private int panelX() { return (width  - PANEL_W) / 2; }
    private int panelY() { return (height - panelH()) / 2; }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // полупрозрачный фон на весь экран
        ctx.fill(0, 0, width, height, 0x88000000);

        int px = panelX();
        int py = panelY();
        int ph = panelH();

        // Панель: тёмный фон
        ctx.fill(px, py, px + PANEL_W, py + ph, 0xF00B0916);

        // Рамка (1 пиксель фиолетовый)
        drawBorder(ctx, px, py, PANEL_W, ph, 0xFF9D6BFF);

        // Заголовок
        ctx.fill(px, py, px + PANEL_W, py + TITLE_H, 0xFF1B1433);
        drawBorder(ctx, px, py, PANEL_W, TITLE_H, 0xFF9D6BFF);

        ctx.drawCenteredTextWithShadow(textRenderer,
                "DONATIL VISUALS", px + PANEL_W / 2, py + 12, 0xFF9D6BFF);
        ctx.drawCenteredTextWithShadow(textRenderer,
                "v2.4.1  |  render-only  |  0 банов",
                px + PANEL_W / 2, py + 28, 0xFF6F688F);

        // Строки модулей
        int ry = py + TITLE_H;
        for (ModEntry e : entries) {
            boolean hovered = mouseX >= px && mouseX < px + PANEL_W
                    && mouseY >= ry && mouseY < ry + ROW_H;
            boolean on = e.get().get();

            if (hovered) ctx.fill(px + 1, ry, px + PANEL_W - 1, ry + ROW_H, 0x22FFFFFF);

            // Цветная точка-индикатор слева
            int dot = on ? 0xFF5CFFC0 : 0xFF6F688F;
            ctx.fill(px + 10, ry + ROW_H / 2 - 3, px + 14, ry + ROW_H / 2 + 3, dot);

            // Название
            ctx.drawTextWithShadow(textRenderer, e.name(),
                    px + 22, ry + 5, on ? 0xFFFFFFFF : 0xFF9B9B9B);
            // Подсказка
            ctx.drawTextWithShadow(textRenderer, e.hint(),
                    px + 22, ry + 15, 0xFF6F688F);

            // ON / OFF справа
            String state = on ? "ON" : "OFF";
            int sc = on ? 0xFF5CFFC0 : 0xFF6F688F;
            ctx.drawTextWithShadow(textRenderer, state,
                    px + PANEL_W - textRenderer.getWidth(state) - 10,
                    ry + ROW_H / 2 - 4, sc);

            // Разделитель
            ctx.fill(px + 1, ry + ROW_H - 1, px + PANEL_W - 1, ry + ROW_H, 0x22FFFFFF);

            ry += ROW_H;
        }

        // Подсказка внизу
        String hint = "RIGHT SHIFT / ESC - закрыть   |   ЛКМ - вкл/выкл";
        ctx.drawCenteredTextWithShadow(textRenderer, hint,
                px + PANEL_W / 2, py + ph - 10, 0xFF3D3666);

        super.render(ctx, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) { // ЛКМ
            int px = panelX();
            int py = panelY();
            int ry = py + TITLE_H;
            for (ModEntry e : entries) {
                if (mouseX >= px && mouseX < px + PANEL_W
                        && mouseY >= ry && mouseY < ry + ROW_H) {
                    e.set().set(!e.get().get()); // переключить
                    return true;
                }
                ry += ROW_H;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Закрыть на RIGHT SHIFT
        if (keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT) {
            close();
            return true;
        }
        // ESC обрабатывается стандартным close() через родительский класс
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldPause() {
        return false; // игра не встаёт на паузу
    }

    /** Рисуем рамку толщиной 1 пиксель */
    private void drawBorder(DrawContext ctx, int x, int y, int w, int h, int color) {
        ctx.fill(x,     y,     x + w, y + 1, color);    // верх
        ctx.fill(x,     y+h-1, x + w, y + h, color);    // низ
        ctx.fill(x,     y,     x + 1, y + h, color);    // лево
        ctx.fill(x+w-1, y,     x + w, y + h, color);    // право
    }
}
