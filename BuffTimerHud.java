package gg.donatil.visuals.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.effect.StatusEffectInstance;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Таймер оставшегося времени активных баффов/дебаффов.
 *
 * Рисуется в правом верхнем углу. Формат:
 *   [иконка] Имя эффекта  00:13
 *
 * Данные берутся из локального объекта игрока — серверу ничего не шлём.
 */
public class BuffTimerHud {

    public boolean enabled = true;

    private final MinecraftClient mc = MinecraftClient.getInstance();

    // Цвета
    private static final int COL_BG_BUFF   = 0x881B1433;
    private static final int COL_BG_DEBUFF = 0x883B0B0B;
    private static final int COL_BUFF      = 0xFF5CFFC0;  // зелёный — бафф
    private static final int COL_DEBUFF    = 0xFFFF5C5C;  // красный — дебафф
    private static final int COL_TIME      = 0xFFC4A8FF;  // фиолетовый — время
    private static final int COL_TIMER_LOW = 0xFFFF9940;  // оранжевый — < 10 сек

    public void render(DrawContext ctx) {
        if (!enabled) return;
        if (mc.player == null) return;

        Collection<StatusEffectInstance> effects = mc.player.getStatusEffects();
        if (effects.isEmpty()) return;

        // Сортируем: сначала баффы (beneficial), потом дебаффы
        List<StatusEffectInstance> sorted = new ArrayList<>(effects);
        sorted.sort((a, b) -> {
            boolean ab = a.getEffectType().value().isBeneficial();
            boolean bb = b.getEffectType().value().isBeneficial();
            if (ab != bb) return ab ? -1 : 1;
            return Integer.compare(b.getDuration(), a.getDuration());
        });

        int sw = ctx.getScaledWindowWidth();
        int rowH  = 20;
        int padX  = 8;
        int padY  = 8;
        int barW  = 160;

        int y = padY;
        for (StatusEffectInstance eff : sorted) {
            boolean beneficial = eff.getEffectType().value().isBeneficial();

            // Имя эффекта через ключ локализации
            String name = eff.getEffectType().value().getName().getString();
            int amplifier = eff.getAmplifier(); // 0 = I, 1 = II ...
            if (amplifier > 0) {
                name = name + " " + toRoman(amplifier + 1);
            }

            // Длительность в секундах
            int totalSec = eff.getDuration() / 20; // 20 тиков = 1 сек
            String timeStr = formatTime(totalSec);

            // Ширина строки текста
            int nameW = mc.textRenderer.getWidth(name);
            int timeW = mc.textRenderer.getWidth(timeStr);
            int rowWidth = Math.max(barW, nameW + timeW + 32);

            int x = sw - rowWidth - padX;

            // Фон строки
            ctx.fill(x, y, x + rowWidth, y + rowH,
                    beneficial ? COL_BG_BUFF : COL_BG_DEBUFF);

            // Полоска прогресса (если есть максимальная длительность ~10 мин = 12000 тиков)
            int maxDur = Math.max(eff.getDuration(), 1);
            float pct = Math.min(1.0f, eff.getDuration() / 6000.0f); // 5 мин = 100%
            int barColor = beneficial ? 0x669D6BFF : 0x66FF5C5C;
            ctx.fill(x, y + rowH - 2, x + (int)(rowWidth * pct), y + rowH, barColor);

            // Имя эффекта (слева)
            ctx.drawTextWithShadow(mc.textRenderer, name,
                    x + 6, y + 6,
                    beneficial ? COL_BUFF : COL_DEBUFF);

            // Таймер (справа)
            int timeColor = totalSec < 10 ? COL_TIMER_LOW : COL_TIME;
            ctx.drawTextWithShadow(mc.textRenderer, timeStr,
                    x + rowWidth - timeW - 6, y + 6, timeColor);

            y += rowH + 2;
        }
    }

    /** Форматирует секунды: если >= 60 — "mm:ss", иначе "s.s" */
    private String formatTime(int totalSec) {
        if (totalSec >= 60) {
            int m = totalSec / 60;
            int s = totalSec % 60;
            return String.format("%d:%02d", m, s);
        }
        return totalSec + "s";
    }

    /** 1->I, 2->II, 3->III, 4->IV, 5->V */
    private String toRoman(int n) {
        String[] r = {"", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"};
        return n >= 0 && n < r.length ? r[n] : String.valueOf(n);
    }
}
