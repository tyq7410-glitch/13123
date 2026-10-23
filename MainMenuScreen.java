package dev.auroravisuals;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class MainMenuScreen extends Screen {
    private final Screen parent;
    private String status = "";

    public MainMenuScreen(Screen parent) {
        super(Text.literal("Aurora Visuals"));
        this.parent = parent;
    }

    private void btn(String label, int x, int y, int w, Runnable r) {
        addDrawableChild(ButtonWidget.builder(Text.literal(label), b -> r.run()).dimensions(x, y, w, 20).build());
    }

    @Override
    protected void init() {
        int L = width / 2 - 155, R = width / 2 + 5, y = 50;
        String[] names = {"HUD", "Info widgets", "Crosshair", "Effects", "Cosmetics", "Theme", "PvP", "Helper"};
        for (int i = 0; i < 8; i++) {
            final int page = i;
            btn(names[i], i % 2 == 0 ? L : R, y + (i / 2) * 24, 150, () -> client.setScreen(new SettingsScreen(this, page)));
        }
        y += 4 * 24 + 6;
        btn("HUD editor (drag)", L, y, 150, () -> client.setScreen(new HudEditorScreen(this)));
        btn("Reset everything", R, y, 150, () -> { Config.reset(); status = "Reset to defaults"; });
        y += 30;
        for (int i = 1; i <= 3; i++) {
            final int n = i;
            btn("Save slot " + i, L + (i - 1) * 105, y, 100, () -> { Config.saveSlot(n); status = "Saved to slot " + n; });
            btn("Load slot " + i, L + (i - 1) * 105, y + 24, 100, () -> {
                status = Config.loadSlot(n) ? "Loaded slot " + n : "Slot " + n + " is empty";
            });
        }
        btn("Close", width / 2 - 75, height - 28, 150, this::close);
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        super.render(ctx, mx, my, delta);
        String t = "AURORA VISUALS";
        ctx.drawText(textRenderer, t, (width - textRenderer.getWidth(t)) / 2, 28, Config.I.accent(), true);
        ctx.fill(width / 2 - 60, 40, width / 2 + 60, 41, Config.I.accent());
        if (!status.isEmpty())
            ctx.drawText(textRenderer, status, (width - textRenderer.getWidth(status)) / 2, height - 42, 0xFFFFFFFF, true);
    }

    @Override
    public void close() {
        Config.save();
        if (client != null) client.setScreen(parent);
    }
}
