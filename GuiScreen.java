package dev.auroravisuals;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

/**
 * Main menu: category sidebar, search, smooth scrolling list of settings, tooltips in the footer.
 * Mouse input is polled (like the HUD editor) so it does not depend on input-event signatures.
 */
public class GuiScreen extends Screen implements Pages.Sink {
    private static final String[] CATS = {"HUD", "Crosshair", "Effects", "Cosmetics", "PvP and PvE", "Screen", "Theme and colors", "Comfort", "Profiles and tools"};
    private static final int[][] CAT_PAGES = {
        {0, 1, 8, 14, 20}, {2, 10}, {3}, {4, 9, 11, 12, 13, 18, 19}, {6, 24, 7, 25}, {15, 21}, {5, 16, 17, 22, 23, 26}, {27}, {}};
    private static final int PROFILES = 8;
    private static final int SW = 100, HH = 26, FH = 30;

    private static final class Row {
        final int kind; // 0 header, 1 toggle, 2 slider, 3 cycle, 4 color, 5 button
        final String name;
        String tip = "";
        int y, h;
        BooleanSupplier bget; Consumer<Boolean> bset;
        float min, max, val; boolean isInt; Consumer<Float> sset;
        String[] opts; IntSupplier iget; IntConsumer iset;
        IntSupplier cget; IntConsumer cset; Runnable enable;
        Runnable action;
        Row(int kind, String name) { this.kind = kind; this.name = name; this.h = kind == 0 ? 16 : 20; }
    }

    private final Screen parent;
    private TextFieldWidget search;
    private List<Row> rows = new ArrayList<>();
    private List<Row> building;
    private int cat = 0, contentH = 0;
    private String query = "", status = "";
    private long statusAt = 0;
    private float scroll = 0, scrollTarget = 0;
    private boolean wasDown = false, dragBar = false;
    private Row activeSlider = null;
    private int px, py, pw, ph, lx0, lx1, ly0, ly1;

    public GuiScreen(Screen parent) {
        super(Text.literal("Aurora Visuals"));
        this.parent = parent;
    }

    // ------------------------------------------------------------------ Pages.Sink
    private Row add(Row r, String tipKey) {
        String t = Tips.of(tipKey);
        r.tip = t == null ? "" : t;
        building.add(r);
        return r;
    }
    @Override public void toggle(String name, BooleanSupplier get, Consumer<Boolean> set) {
        Row r = add(new Row(1, name), name); r.bget = get; r.bset = set;
    }
    @Override public void slider(String name, float min, float max, float cur, boolean isInt, Consumer<Float> set) {
        Row r = add(new Row(2, name), name); r.min = min; r.max = max; r.val = cur; r.isInt = isInt; r.sset = set;
    }
    @Override public void cycle(String name, String[] opts, IntSupplier get, IntConsumer set) {
        Row r = add(new Row(3, name), name); r.opts = opts; r.iget = get; r.iset = set;
    }
    @Override public void palette(String name, IntSupplier get, IntConsumer set, Runnable enable) {
        Row r = add(new Row(4, name), name); r.cget = get; r.cset = set; r.enable = enable;
    }
    @Override public void button(String label, String tipKey, Runnable r0) {
        Row r = add(new Row(5, label), tipKey); r.action = r0;
    }
    @Override public void open(Screen s) { client.setScreen(s); }
    @Override public Screen self() { return this; }
    @Override public void refresh() { rebuild(); }

    private void header(String title) { building.add(new Row(0, title)); }

    private void rawButton(String label, String tip, Runnable r0) {
        Row r = new Row(5, label); r.tip = tip; r.action = r0; building.add(r);
    }

    // ------------------------------------------------------------------ rows
    private void note(String msg) { status = msg; statusAt = System.currentTimeMillis(); }

    private void profileRows() {
        header("Quick profiles");
        rawButton("Minimal", "Только FPS и CPS, всё остальное выключено. Тема и позиции сохраняются.", () -> { Config.applyProfile(0); note("Profile: Minimal"); rebuild(); });
        rawButton("PvP", "PvP-функции, счётчики, звук удара. Тема и позиции сохраняются.", () -> { Config.applyProfile(1); note("Profile: PvP"); rebuild(); });
        rawButton("Cosmetic", "Крылья, нимб, аура, след, лепестки. Тема и позиции сохраняются.", () -> { Config.applyProfile(2); note("Profile: Cosmetic"); rebuild(); });
        rawButton("Comfort", "Спокойный режим, высокий контраст, меньше движения, компактный HUD.", () -> { Config.applyComfort(); note("Profile: Comfort"); rebuild(); });
        header("Quick slots");
        for (int i = 1; i <= 3; i++) {
            final int n = i;
            rawButton("Save slot " + i, "Сохранить текущие настройки в быстрый слот " + i + ".", () -> { Config.saveSlot(n); note("Saved to slot " + n); });
            rawButton("Load slot " + i, "Загрузить настройки из быстрого слота " + i + ".", () -> { note(Config.loadSlot(n) ? "Loaded slot " + n : "Slot " + n + " is empty"); rebuild(); });
        }
        header("Tools");
        rawButton("HUD editor", "Редактор: перетаскивайте блоки HUD мышкой.", () -> client.setScreen(new HudEditorScreen(this)));
        rawButton("Config manager", "Сохранить конфиг под именем, загрузить, удалить, поделиться через буфер обмена.", () -> client.setScreen(new ConfigScreen(this)));
        rawButton("Reset everything", "Сбросить ВСЕ настройки на стандартные.", () -> { Config.reset(); note("Reset to defaults"); rebuild(); });
    }

    private void rebuild() {
        building = new ArrayList<>();
        String q = query.trim().toLowerCase();
        if (!q.isEmpty()) {
            for (int p = 0; p < Pages.TITLES.length; p++) {
                List<Row> all = new ArrayList<>();
                List<Row> saved = building;
                building = all;
                Pages.build(p, this);
                building = saved;
                for (Row r : all)
                    if (r.kind != 0 && (r.name.toLowerCase().contains(q) || r.tip.toLowerCase().contains(q))) building.add(r);
            }
        } else if (cat == PROFILES) {
            profileRows();
        } else {
            for (int p : CAT_PAGES[cat]) {
                header(Pages.TITLES[p]);
                Pages.build(p, this);
            }
        }
        rows = building;
        int y = 0;
        for (Row r : rows) { r.y = y; y += r.h; }
        contentH = y;
        scrollTarget = Math.max(0, Math.min(scrollTarget, Math.max(0, contentH - (ly1 - ly0))));
    }

    @Override
    protected void init() {
        pw = Math.min(width - 20, 470);
        ph = Math.min(height - 16, 290);
        px = (width - pw) / 2;
        py = (height - ph) / 2;
        lx0 = px + SW + 10; lx1 = px + pw - 14;
        ly0 = py + HH + 6; ly1 = py + ph - FH - 4;
        search = new TextFieldWidget(textRenderer, px + pw - 178, py + 5, 150, 16, Text.literal("Search"));
        search.setMaxLength(32);
        search.setPlaceholder(Text.literal("Поиск / Search"));
        search.setText(query);
        addSelectableChild(search);
        rebuild();
    }

    // ------------------------------------------------------------------ input
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        scrollTarget = Math.max(0, Math.min(scrollTarget - (float) verticalAmount * 28f, Math.max(0, contentH - (ly1 - ly0))));
        return true;
    }

    private boolean in(int mx, int my, int x0, int y0, int x1, int y1) { return mx >= x0 && mx < x1 && my >= y0 && my < y1; }

    private void apply(Row r, int mx) {
        int sx0 = sliderX0(), sx1 = sliderX1();
        float f = Math.max(0f, Math.min(1f, (mx - sx0) / (float) Math.max(1, sx1 - sx0)));
        float v = r.min + f * (r.max - r.min);
        if (r.isInt) v = Math.round(v);
        r.val = v;
        r.sset.accept(v);
    }
    private int sliderX0() { return lx0 + (lx1 - lx0) * 45 / 100; }
    private int sliderX1() { return lx1 - 46; }

    private void click(Row r, int mx) {
        switch (r.kind) {
            case 1 -> r.bset.accept(!r.bget.getAsBoolean());
            case 2 -> { activeSlider = r; apply(r, mx); }
            case 3 -> r.iset.accept((r.iget.getAsInt() + 1) % r.opts.length);
            case 4 -> client.setScreen(new ColorScreen(this, r.name, r.cget, v -> { r.cset.accept(v); if (r.enable != null) r.enable.run(); }));
            case 5 -> r.action.run();
            default -> {}
        }
    }

    // ------------------------------------------------------------------ drawing
    private String fmt(Row r) { return r.isInt ? String.valueOf(Math.round(r.val)) : String.format("%.2f", r.val); }

    private void wrapped(DrawContext ctx, String text, int x, int y, int maxW, int color) {
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();
        int yy = y, lines = 0;
        for (String w : words) {
            String test = line.length() == 0 ? w : line + " " + w;
            if (textRenderer.getWidth(test) > maxW && line.length() > 0) {
                ctx.drawText(textRenderer, line.toString(), x, yy, color, false);
                yy += 10; lines++;
                line = new StringBuilder(w);
                if (lines >= 2) return;
            } else line = new StringBuilder(test);
        }
        if (line.length() > 0 && lines < 2) ctx.drawText(textRenderer, line.toString(), x, yy, color, false);
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        super.render(ctx, mx, my, delta);
        if (client == null) return;
        Config c = Config.I;
        int acc = c.accent();

        // input state (polled)
        long win = client.getWindow().getHandle();
        boolean down = GLFW.glfwGetMouseButton(win, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        boolean pressed = down && !wasDown;
        wasDown = down;
        if (!down) { activeSlider = null; dragBar = false; }

        // search changes
        String qNow = search.getText();
        if (!qNow.equals(query)) { query = qNow; scrollTarget = 0; scroll = 0; rebuild(); }

        // keyboard scrolling when the search box is not focused
        if (!search.isFocused()) {
            if (GLFW.glfwGetKey(win, GLFW.GLFW_KEY_DOWN) == GLFW.GLFW_PRESS) scrollTarget += 6;
            if (GLFW.glfwGetKey(win, GLFW.GLFW_KEY_UP) == GLFW.GLFW_PRESS) scrollTarget -= 6;
            if (GLFW.glfwGetKey(win, GLFW.GLFW_KEY_PAGE_DOWN) == GLFW.GLFW_PRESS) scrollTarget += 18;
            if (GLFW.glfwGetKey(win, GLFW.GLFW_KEY_PAGE_UP) == GLFW.GLFW_PRESS) scrollTarget -= 18;
        }
        int maxScroll = Math.max(0, contentH - (ly1 - ly0));

        // panel
        ctx.fill(px - 1, py - 1, px + pw + 1, py + ph + 1, 0xFF000000 | (acc & 0xFFFFFF));
        ctx.fill(px, py, px + pw, py + ph, 0xF2101624);
        ctx.fill(px, py, px + pw, py + HH, 0xFF0A0E1A);
        ctx.fill(px, py + HH, px + pw, py + HH + 1, 0x55000000 | (acc & 0xFFFFFF));
        ctx.fill(px, py + HH + 1, px + SW, py + ph, 0xFF0B0F1C);
        ctx.drawText(textRenderer, "AURORA VISUALS", px + 10, py + 9, acc, true);
        if (!status.isEmpty() && System.currentTimeMillis() - statusAt < 3500)
            ctx.drawText(textRenderer, status, px + 112, py + 9, 0xFFFFFFFF, true);
        boolean closeHover = in(mx, my, px + pw - 22, py + 4, px + pw - 6, py + 20);
        ctx.drawText(textRenderer, "x", px + pw - 17, py + 9, closeHover ? 0xFFFF5555 : 0xFFB0B8C8, true);
        search.render(ctx, mx, my, delta);
        if (pressed && closeHover) { close(); return; }

        // sidebar
        int itemH = Math.max(14, Math.min(22, (ph - HH - 12) / CATS.length));
        for (int i = 0; i < CATS.length; i++) {
            int y = py + HH + 6 + i * itemH;
            boolean sel = i == cat && query.trim().isEmpty();
            boolean hov = in(mx, my, px + 4, y, px + SW - 4, y + itemH - 2);
            if (sel) { ctx.fill(px + 4, y, px + SW - 4, y + itemH - 2, 0x33000000 | (acc & 0xFFFFFF)); ctx.fill(px + 4, y, px + 7, y + itemH - 2, 0xFF000000 | (acc & 0xFFFFFF)); }
            else if (hov) ctx.fill(px + 4, y, px + SW - 4, y + itemH - 2, 0x22FFFFFF);
            ctx.drawText(textRenderer, CATS[i], px + 12, y + (itemH - 10) / 2, sel ? 0xFFFFFFFF : 0xFFA9B4C8, false);
            if (pressed && hov) {
                cat = i; scroll = 0; scrollTarget = 0;
                search.setText(""); query = "";
                rebuild();
            }
        }

        // list (smooth scrolling + clipping)
        scrollTarget = Math.max(0, Math.min(scrollTarget, maxScroll));
        scroll += (scrollTarget - scroll) * 0.35f;
        if (Math.abs(scrollTarget - scroll) < 0.4f) scroll = scrollTarget;
        ctx.enableScissor(lx0 - 4, ly0, lx1 + 8, ly1);
        Row hovered = null;
        boolean inList = in(mx, my, lx0 - 4, ly0, lx1 + 2, ly1);
        for (Row r : rows) {
            int y = ly0 + r.y - (int) scroll;
            if (y + r.h < ly0 || y > ly1) continue;
            boolean hov = inList && my >= y && my < y + r.h && r.kind != 0;
            if (hov) hovered = r;
            drawRow(ctx, r, y, hov, acc, mx);
            if (pressed && hov) click(r, mx);
        }
        ctx.disableScissor();
        if (activeSlider != null && down) apply(activeSlider, mx);
        if (rows.isEmpty())
            ctx.drawText(textRenderer, "Ничего не найдено / Nothing found", lx0 + 6, ly0 + 8, 0xFF8FA0BC, false);

        // scrollbar
        if (maxScroll > 0) {
            int bx = lx1 + 4, bh = ly1 - ly0;
            ctx.fill(bx, ly0, bx + 4, ly1, 0x33FFFFFF);
            int th = Math.max(16, bh * bh / contentH);
            int ty = ly0 + (int) ((bh - th) * (scroll / maxScroll));
            ctx.fill(bx, ty, bx + 4, ty + th, 0xFF000000 | (acc & 0xFFFFFF));
            if (pressed && in(mx, my, bx - 3, ly0, bx + 8, ly1)) dragBar = true;
            if (dragBar && down) scrollTarget = Math.max(0, Math.min(maxScroll, (my - ly0 - th / 2f) / (bh - th) * maxScroll));
        }

        // footer: description of the hovered setting (Russian)
        ctx.fill(px, py + ph - FH, px + pw, py + ph, 0xFF0A0E1A);
        ctx.fill(px, py + ph - FH, px + pw, py + ph - FH + 1, 0x33FFFFFF);
        String tip = hovered != null && !hovered.tip.isEmpty() ? hovered.tip : "Наведи курсор на функцию, и здесь появится описание. Колёсико или стрелки: прокрутка.";
        wrapped(ctx, tip, px + 10, py + ph - FH + 5, pw - 20, hovered != null ? 0xFFE6ECF8 : 0xFF7F8CA6);
    }

    private void drawRow(DrawContext ctx, Row r, int y, boolean hov, int acc, int mx) {
        int a = acc & 0xFFFFFF;
        if (r.kind == 0) {
            ctx.drawText(textRenderer, r.name, lx0 + 2, y + 4, 0xFF000000 | a, true);
            ctx.fill(lx0, y + 14, lx1, y + 15, 0x33FFFFFF);
            return;
        }
        if (hov) ctx.fill(lx0 - 2, y, lx1 + 2, y + r.h - 1, 0x1FFFFFFF);
        ctx.drawText(textRenderer, r.kind == 5 ? r.name : r.name, lx0 + 6, y + 6, r.kind == 5 ? (0xFF000000 | a) : 0xFFE6ECF8, false);
        switch (r.kind) {
            case 1 -> {
                boolean on = r.bget.getAsBoolean();
                int x = lx1 - 28;
                ctx.fill(x, y + 5, x + 24, y + 15, on ? (0xFF000000 | a) : 0xFF3A4254);
                ctx.fill(x + (on ? 14 : 2), y + 7, x + (on ? 22 : 10), y + 13, 0xFFFFFFFF);
            }
            case 2 -> {
                int x0 = sliderX0(), x1 = sliderX1();
                float f = (r.val - r.min) / Math.max(0.0001f, r.max - r.min);
                f = Math.max(0f, Math.min(1f, f));
                ctx.fill(x0, y + 8, x1, y + 12, 0xFF2A3042);
                ctx.fill(x0, y + 8, x0 + (int) ((x1 - x0) * f), y + 12, 0xFF000000 | a);
                int kx = x0 + (int) ((x1 - x0) * f);
                ctx.fill(kx - 2, y + 5, kx + 2, y + 15, 0xFFFFFFFF);
                String v = fmt(r);
                ctx.drawText(textRenderer, v, lx1 - 4 - textRenderer.getWidth(v), y + 6, 0xFFE6ECF8, false);
            }
            case 3 -> {
                String v = "< " + r.opts[r.iget.getAsInt() % r.opts.length] + " >";
                ctx.drawText(textRenderer, v, lx1 - 4 - textRenderer.getWidth(v), y + 6, 0xFF000000 | a, false);
            }
            case 4 -> {
                int x = lx1 - 26;
                ctx.fill(x - 1, y + 4, x + 21, y + 16, 0xFFFFFFFF);
                ctx.fill(x, y + 5, x + 20, y + 15, 0xFF000000 | (r.cget.getAsInt() & 0xFFFFFF));
            }
            case 5 -> ctx.drawText(textRenderer, ">", lx1 - 12, y + 6, 0xFF000000 | a, false);
            default -> {}
        }
    }

    @Override
    public void close() {
        Config.save();
        if (client != null) client.setScreen(parent);
    }
}
