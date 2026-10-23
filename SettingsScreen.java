package dev.auroravisuals;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class SettingsScreen extends Screen {
    private final Screen parent;
    private int page = 0, n = 0;

    public SettingsScreen(Screen parent) { this(parent, 0); }

    public SettingsScreen(Screen parent, int page) {
        super(Text.literal("Aurora Visuals"));
        this.parent = parent;
        this.page = page;
    }

    @Override
    protected void init() {
        n = 0;
        Config c = Config.I;
        String[] tabs = {"HUD", "Info", "Cross", "FX", "Cosm", "Theme", "PvP", "Help"};
        for (int i = 0; i < 8; i++) {
            final int p = i;
            addDrawableChild(ButtonWidget.builder(Text.literal((page == i ? "> " : "") + tabs[i]), b -> { page = p; clearAndInit(); })
                .dimensions(width / 2 - 155 + i * 39, 10, 37, 20).build());
        }
        if (page == 0) {
            toggle("Watermark", () -> c.watermark, v -> c.watermark = v);
            toggle("FPS", () -> c.fps, v -> c.fps = v);
            toggle("CPS", () -> c.cps, v -> c.cps = v);
            toggle("Ping", () -> c.ping, v -> c.ping = v);
            toggle("Coordinates", () -> c.coords, v -> c.coords = v);
            toggle("Keystrokes", () -> c.keystrokes, v -> c.keystrokes = v);
            toggle("Text shadow", () -> c.textShadow, v -> c.textShadow = v);
            slider("HUD scale", 0.5f, 2f, c.hudScale, false, v -> c.hudScale = v);
            toggle("Accent bar", () -> c.accentBar, v -> c.accentBar = v);
            toggle("Panel border", () -> c.hudBorder, v -> c.hudBorder = v);
            cycle("Text color", Config.TEXT_MODES, () -> c.textMode, v -> c.textMode = v);
            addDrawableChild(ButtonWidget.builder(Text.literal("Open HUD editor"), b -> client.setScreen(new HudEditorScreen(this)))
                .dimensions(px(), py(), 150, 20).build());
            n++;
        } else if (page == 1) {
            toggle("Speed", () -> c.speed, v -> c.speed = v);
            toggle("Direction", () -> c.direction, v -> c.direction = v);
            toggle("Clock", () -> c.clock, v -> c.clock = v);
            toggle("RAM", () -> c.memory, v -> c.memory = v);
            toggle("Server IP", () -> c.server, v -> c.server = v);
            toggle("Combo counter", () -> c.combo, v -> c.combo = v);
            toggle("Armor HUD", () -> c.armorHud, v -> c.armorHud = v);
            toggle("Potion HUD", () -> c.potionHud, v -> c.potionHud = v);
            toggle("Target HUD", () -> c.targetHud, v -> c.targetHud = v);
            toggle("Move state", () -> c.state, v -> c.state = v);
            toggle("Totem/Gapple count", () -> c.itemCount, v -> c.itemCount = v);
        } else if (page == 2) {
            toggle("Custom crosshair", () -> c.crosshair, v -> c.crosshair = v);
            toggle("Cross", () -> c.chCross, v -> c.chCross = v);
            toggle("Center dot", () -> c.chDot, v -> c.chDot = v);
            toggle("Outline", () -> c.chOutline, v -> c.chOutline = v);
            toggle("Rainbow color", () -> c.chRainbow, v -> c.chRainbow = v);
            toggle("Dynamic gap", () -> c.chDynamic, v -> c.chDynamic = v);
            toggle("Circle", () -> c.chCircle, v -> c.chCircle = v);
            slider("Size", 1, 20, c.chSize, true, v -> c.chSize = Math.round(v));
            slider("Gap", 0, 15, c.chGap, true, v -> c.chGap = Math.round(v));
            slider("Thickness", 1, 6, c.chThick, true, v -> c.chThick = Math.round(v));
            toggle("Hitmarker", () -> c.hitmarker, v -> c.hitmarker = v);
            slider("Hitmarker size", 2, 12, c.hitSize, true, v -> c.hitSize = Math.round(v));
            slider("Hitmarker time", 2, 30, c.hitTicks, true, v -> c.hitTicks = Math.round(v));
        } else if (page == 3) {
            toggle("Trail", () -> c.trail, v -> c.trail = v);
            toggle("Trail in 1st person", () -> c.trailFirstPerson, v -> c.trailFirstPerson = v);
            slider("Trail density", 1, 8, c.trailDensity, true, v -> c.trailDensity = Math.round(v));
            toggle("China hat", () -> c.hat, v -> c.hat = v);
            slider("Hat size", 0.3f, 1.2f, c.hatSize, false, v -> c.hatSize = v);
            slider("Hat height", 0f, 0.8f, c.hatHeight, false, v -> c.hatHeight = v);
            slider("Hat points", 6, 30, c.hatPoints, true, v -> c.hatPoints = Math.round(v));
            toggle("Hit particles", () -> c.hitParticles, v -> c.hitParticles = v);
            slider("Particle count", 1, 30, c.hitCount, true, v -> c.hitCount = Math.round(v));
            addDrawableChild(ButtonWidget.builder(Text.literal("Hit type: " + Extras.HIT_TYPES[c.hitType]), btn -> {
                c.hitType = (c.hitType + 1) % Extras.HIT_TYPES.length;
                btn.setMessage(Text.literal("Hit type: " + Extras.HIT_TYPES[c.hitType]));
            }).dimensions(px(), py(), 150, 20).build());
            n++;
            cycle("Hit sound", Extras.SOUNDS, () -> c.hitSound, v -> c.hitSound = v);
            cycle("Particle color", Extras.COLORS, () -> c.pColor, v -> c.pColor = v);
            toggle("No view bobbing", () -> c.noBob, v -> c.noBob = v);
            toggle("No sprint FOV", () -> c.noFovFx, v -> c.noFovFx = v);
            slider("Zoom FOV (key C)", 30, 70, c.zoomFov, true, v -> c.zoomFov = Math.round(v));
        } else if (page == 4) {
            toggle("Show in 1st person", () -> c.cosFirst, v -> c.cosFirst = v);
            toggle("Halo", () -> c.halo, v -> c.halo = v);
            slider("Halo size", 0.15f, 0.8f, c.haloSize, false, v -> c.haloSize = v);
            slider("Halo height", 0f, 0.8f, c.haloHeight, false, v -> c.haloHeight = v);
            toggle("Aura", () -> c.aura, v -> c.aura = v);
            slider("Aura orbs", 1, 16, c.auraCount, true, v -> c.auraCount = Math.round(v));
            slider("Aura radius", 0.4f, 1.6f, c.auraRadius, false, v -> c.auraRadius = v);
            slider("Aura speed", 0.2f, 4f, c.auraSpeed, false, v -> c.auraSpeed = v);
            toggle("Footsteps", () -> c.footsteps, v -> c.footsteps = v);
            toggle("Wings", () -> c.wings, v -> c.wings = v);
            slider("Wing size", 0.5f, 2f, c.wingSize, false, v -> c.wingSize = v);
            toggle("Kill effect", () -> c.killEffect, v -> c.killEffect = v);
        } else if (page == 5) {
            addDrawableChild(ButtonWidget.builder(Text.literal("Preset: " + Config.PRESETS[c.preset]), btn -> {
                Config.applyPreset((c.preset + 1) % Config.PRESETS.length);
                clearAndInit();
            }).dimensions(px(), py(), 150, 20).build());
            n++;
            slider("Accent hue", 0, 1, c.hue, false, v -> c.hue = v);
            toggle("Rainbow theme", () -> c.rainbow, v -> c.rainbow = v);
            slider("Rainbow speed", 0.1f, 5f, c.rainbowSpeed, false, v -> c.rainbowSpeed = v);
            slider("Panel opacity", 0, 255, c.bgOpacity, true, v -> c.bgOpacity = Math.round(v));
        } else if (page == 6) {
            toggle("Low HP warning", () -> c.lowHp, v -> c.lowHp = v);
            slider("Low HP at", 2, 18, c.lowHpThreshold, true, v -> c.lowHpThreshold = Math.round(v));
            toggle("Target HUD", () -> c.targetHud, v -> c.targetHud = v);
            toggle("Target armor", () -> c.targetArmor, v -> c.targetArmor = v);
            toggle("Distance + HP", () -> c.showReach, v -> c.showReach = v);
            toggle("Combat timer", () -> c.combatTimer, v -> c.combatTimer = v);
            slider("Combat seconds", 5, 60, c.combatSecs, true, v -> c.combatSecs = Math.round(v));
            toggle("Combo counter", () -> c.combo, v -> c.combo = v);
            toggle("Totem pops", () -> c.pops, v -> c.pops = v);
            toggle("Kill effect", () -> c.killEffect, v -> c.killEffect = v);
            toggle("Hitmarker", () -> c.hitmarker, v -> c.hitmarker = v);
            cycle("Hit sound", Extras.SOUNDS, () -> c.hitSound, v -> c.hitSound = v);
        } else if (page == 7) {
            toggle("Cooldown HUD", () -> c.cdHud, v -> c.cdHud = v);
            toggle("Ready ding", () -> c.cdReady, v -> c.cdReady = v);
            toggle("Item counts", () -> c.itemCount, v -> c.itemCount = v);
            toggle("Potion HUD", () -> c.potionHud, v -> c.potionHud = v);
            toggle("Armor HUD (red<15%)", () -> c.armorHud, v -> c.armorHud = v);
            toggle("Move state", () -> c.state, v -> c.state = v);
            toggle("Server IP", () -> c.server, v -> c.server = v);
            toggle("Clock", () -> c.clock, v -> c.clock = v);
        } else if (page == 8) {
            toggle("Day counter", () -> c.day, v -> c.day = v);
            toggle("World time", () -> c.worldTime, v -> c.worldTime = v);
            toggle("Biome", () -> c.biome, v -> c.biome = v);
            toggle("Light level", () -> c.light, v -> c.light = v);
            toggle("Yaw / Pitch", () -> c.angles, v -> c.angles = v);
            toggle("Held item", () -> c.heldItem, v -> c.heldItem = v);
            toggle("Nether coords", () -> c.netherCoords, v -> c.netherCoords = v);
            toggle("HP number", () -> c.hpText, v -> c.hpText = v);
            toggle("Food number", () -> c.hungerText, v -> c.hungerText = v);
            toggle("XP level", () -> c.xpLevel, v -> c.xpLevel = v);
            toggle("Session time", () -> c.session, v -> c.session = v);
            toggle("FPS color", () -> c.fpsColor, v -> c.fpsColor = v);
            toggle("Ping color", () -> c.pingColor, v -> c.pingColor = v);
            toggle("Mouse keys", () -> c.keysMouse, v -> c.keysMouse = v);
        } else if (page == 9) {
            cycle("Particle shape", Extras.SHAPES, () -> c.pShape, v -> c.pShape = v);
            toggle("Waist ring", () -> c.ring, v -> c.ring = v);
            slider("Ring radius", 0.3f, 1.5f, c.ringRadius, false, v -> c.ringRadius = v);
            toggle("Helix", () -> c.helix, v -> c.helix = v);
            toggle("Rain cloud", () -> c.cloud, v -> c.cloud = v);
            toggle("Jump rings", () -> c.jumpRings, v -> c.jumpRings = v);
            toggle("Level-up burst", () -> c.lvlBurst, v -> c.lvlBurst = v);
            toggle("Fireflies", () -> c.fireflies, v -> c.fireflies = v);
        } else {
            toggle("Compass", () -> c.compass, v -> c.compass = v);
            toggle("Low food warning", () -> c.lowFood, v -> c.lowFood = v);
            toggle("Attack bar", () -> c.atkBar, v -> c.atkBar = v);
            toggle("Screen frame", () -> c.frame, v -> c.frame = v);
            slider("Frame thickness", 1, 10, c.frameThick, true, v -> c.frameThick = Math.round(v));
            toggle("Hurt flash", () -> c.hurtFlash, v -> c.hurtFlash = v);
            toggle("Kill counter", () -> c.kills, v -> c.kills = v);
            toggle("Best combo", () -> c.bestCombo, v -> c.bestCombo = v);
            toggle("Red on target", () -> c.chTargetColor, v -> c.chTargetColor = v);
            slider("Crosshair alpha", 30, 255, c.chAlpha, true, v -> c.chAlpha = Math.round(v));
        }
        int[][] link = {{1, 8}, {4, 9}, {6, 10}, {8, 1}, {9, 4}, {10, 6}};
        for (int[] l : link) {
            if (page != l[0]) continue;
            final int to = l[1];
            addDrawableChild(ButtonWidget.builder(Text.literal(page < 8 ? "More >" : "< Back"), b -> { page = to; clearAndInit(); })
                .dimensions(width / 2 - 155, height - 28, 70, 20).build());
        }
        addDrawableChild(ButtonWidget.builder(Text.literal("Done"), b -> close())
            .dimensions(width / 2 - 75, height - 28, 150, 20).build());
    }

    private int px() { return n % 2 == 0 ? width / 2 - 155 : width / 2 + 5; }
    private int py() { return 40 + (n / 2) * 24; }

    private void toggle(String name, BooleanSupplier get, Consumer<Boolean> set) {
        ButtonWidget b = ButtonWidget.builder(Text.literal(name + ": " + (get.getAsBoolean() ? "ON" : "OFF")), btn -> {
            set.accept(!get.getAsBoolean());
            btn.setMessage(Text.literal(name + ": " + (get.getAsBoolean() ? "ON" : "OFF")));
        }).dimensions(px(), py(), 150, 20).build();
        addDrawableChild(b);
        n++;
    }

    private void cycle(String name, String[] opts, java.util.function.IntSupplier get, java.util.function.IntConsumer set) {
        addDrawableChild(ButtonWidget.builder(Text.literal(name + ": " + opts[get.getAsInt()]), btn -> {
            set.accept((get.getAsInt() + 1) % opts.length);
            btn.setMessage(Text.literal(name + ": " + opts[get.getAsInt()]));
        }).dimensions(px(), py(), 150, 20).build());
        n++;
    }

    private void slider(String name, float min, float max, float cur, boolean isInt, Consumer<Float> set) {
        addDrawableChild(new Slide(px(), py(), name, min, max, cur, isInt, set));
        n++;
    }

    private static class Slide extends SliderWidget {
        private final String name; private final float min, max; private final boolean isInt; private final Consumer<Float> set;
        Slide(int x, int y, String name, float min, float max, float cur, boolean isInt, Consumer<Float> set) {
            super(x, y, 150, 20, Text.empty(), (cur - min) / (max - min));
            this.name = name; this.min = min; this.max = max; this.isInt = isInt; this.set = set;
            updateMessage();
        }
        private float val() { return (float) (min + (max - min) * value); }
        @Override protected void updateMessage() {
            setMessage(Text.literal(name + ": " + (isInt ? String.valueOf(Math.round(val())) : String.format("%.2f", val()))));
        }
        @Override protected void applyValue() { set.accept(val()); }
    }

    @Override
    public void close() {
        Config.save();
        if (client != null) client.setScreen(parent);
    }
}
