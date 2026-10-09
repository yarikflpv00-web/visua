package gg.donatil.visuals;

import gg.donatil.visuals.hud.BuffTimerHud;
import gg.donatil.visuals.hud.FpsHud;
import gg.donatil.visuals.hud.KeystrokesHud;
import gg.donatil.visuals.ui.ClickGuiScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

/**
 * Donatil Visuals - точка входа.
 *
 * Все изменения существуют ТОЛЬКО на этапе отрисовки кадра.
 * Серверу не отправляется ни одного лишнего пакета.
 */
public class DonatilVisuals implements ClientModInitializer {

    public static final String MOD_ID = "donatil-visuals";

    // HUD-модули
    public static final FpsHud        FPS_HUD   = new FpsHud();
    public static final KeystrokesHud KEYS_HUD  = new KeystrokesHud();
    public static final BuffTimerHud  BUFF_HUD  = new BuffTimerHud();

    // fullbright
    public static boolean fullbrightEnabled = false;
    private static double savedGamma = 1.0;

    private static KeyBinding guiKey;

    @Override
    public void onInitializeClient() {

        // Клавиша RIGHT SHIFT — открыть/закрыть меню
        guiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.donatil-visuals.gui",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                "category.donatil-visuals"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(DonatilVisuals::onTick);

        // Рисуем HUD поверх игры каждый кадр
        HudRenderCallback.EVENT.register((drawContext, tickDeltaManager) -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            // Не рисуем поверх экрана инвентаря и т.д.
            if (mc.player == null) return;
            if (mc.currentScreen != null && !(mc.currentScreen instanceof ClickGuiScreen)) return;
            if (mc.currentScreen instanceof ClickGuiScreen) return; // GUI сам рисует

            FPS_HUD.render(drawContext);
            KEYS_HUD.render(drawContext);
            BUFF_HUD.render(drawContext);
        });
    }

    private static void onTick(MinecraftClient mc) {
        // Открыть/закрыть меню на правый шифт
        while (guiKey.wasPressed()) {
            if (mc.player == null) continue;
            if (mc.currentScreen instanceof ClickGuiScreen) {
                mc.setScreen(null);     // закрыть
            } else if (mc.currentScreen == null) {
                mc.setScreen(new ClickGuiScreen()); // открыть
            }
        }

        // Fullbright: меняем гамму только локально
        if (fullbrightEnabled) {
            double cur = mc.options.getGamma().getValue();
            if (cur < 5.0) {
                savedGamma = cur;
                mc.options.getGamma().setValue(16.0);
            }
        } else {
            double cur = mc.options.getGamma().getValue();
            if (cur > 1.0) {
                mc.options.getGamma().setValue(savedGamma);
            }
        }
    }
}
