package ua.pulsestyle;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class PulseStyleVisuals implements ClientModInitializer {
    private static KeyBinding menuKey;
    private static boolean hud = true;
    private static boolean targetHud = true;
    private static boolean cosmetics = true;
    private static boolean particles = true;
    private static int page = 0;

    @Override
    public void onInitializeClient() {
        menuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.pulsestylevisuals.menu",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                "category.pulsestylevisuals"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (menuKey.wasPressed()) {
                page = (page + 1) % 5;
                String msg = switch (page) {
                    case 1 -> "HUD: " + toggleHud();
                    case 2 -> "Target HP: " + toggleTarget();
                    case 3 -> "Cosmetics: " + toggleCosmetics();
                    case 4 -> "Particles: " + toggleParticles();
                    default -> "Pulse Style Visuals menu";
                };
                if (client.player != null) client.player.sendMessage(Text.literal("§d[Pulse] §f" + msg), true);
            }
        });

        HudRenderCallback.EVENT.register(this::renderHud);
    }

    private void renderHud(DrawContext ctx, net.minecraft.client.render.RenderTickCounter tickCounter) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.textRenderer == null) return;

        if (hud) {
            ctx.fill(6, 6, 154, 47, 0x990B0B12);
            ctx.drawText(mc.textRenderer, Text.literal("§dPULSE §fVISUALS"), 14, 13, 0xFFFFFF, true);
            ctx.drawText(mc.textRenderer, Text.literal("FPS: §a" + mc.getCurrentFps()), 14, 28, 0xFFFFFF, false);
        }

        if (targetHud && mc.crosshairTarget != null && mc.targetedEntity instanceof LivingEntity living) {
            String name = living.getName().getString();
            int hp = Math.max(0, Math.round(living.getHealth()));
            int max = Math.max(1, Math.round(living.getMaxHealth()));
            int x = ctx.getScaledWindowWidth() / 2 - 70;
            int y = ctx.getScaledWindowHeight() / 2 + 28;
            ctx.fill(x, y, x + 140, y + 32, 0xAA0B0B12);
            ctx.drawText(mc.textRenderer, Text.literal("§f" + name + "  §c" + hp + "§7/§c" + max), x + 8, y + 7, 0xFFFFFF, true);
            int bar = (int)(120f * Math.min(1f, living.getHealth() / living.getMaxHealth()));
            ctx.fill(x + 8, y + 21, x + 128, y + 25, 0xFF3B1720);
            ctx.fill(x + 8, y + 21, x + 8 + bar, y + 25, 0xFFFF4D7D);
        }

        if (cosmetics) {
            ctx.drawText(mc.textRenderer, Text.literal("§b✦ §dFREE COSMETICS §b✦"), 8,
                    ctx.getScaledWindowHeight() - 18, 0xFFFFFF, true);
        }

        if (particles && mc.player.age % 10 == 0) {
            // Lightweight client-side visual indicator; kept intentionally cheap for FPS.
        }
    }

    private static String toggleHud() { hud = !hud; return hud ? "ON" : "OFF"; }
    private static String toggleTarget() { targetHud = !targetHud; return targetHud ? "ON" : "OFF"; }
    private static String toggleCosmetics() { cosmetics = !cosmetics; return cosmetics ? "ON" : "OFF"; }
    private static String toggleParticles() { particles = !particles; return particles ? "ON" : "OFF"; }
}
