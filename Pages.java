package dev.auroravisuals;

import net.minecraft.client.gui.screen.Screen;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

/** Every setting of the mod, defined once. A Sink (the menu) turns these definitions into widgets. */
public final class Pages {
    public interface Sink {
        void toggle(String name, BooleanSupplier get, Consumer<Boolean> set);
        void slider(String name, float min, float max, float cur, boolean isInt, Consumer<Float> set);
        void cycle(String name, String[] opts, IntSupplier get, IntConsumer set);
        void palette(String name, IntSupplier get, IntConsumer set, Runnable enable);
        void button(String label, String tipKey, Runnable r);
        void open(Screen screen);
        Screen self();
        void refresh();
    }

    public static final String[] TITLES = {"HUD", "Info widgets", "Crosshair", "Effects", "Cosmetics", "Theme", "PvP", "Helper", "Info 2", "Cosmetics 2", "Visuals 2", "Cosmetics 3", "Cosmetics 4", "Cosmetics 5", "Widgets", "Screen effects", "Colors 1", "Colors 2", "Cosmetics 6", "Cosmetics 7", "Widgets 2", "Screen effects 2", "Colors 3", "Colors 4", "PvP 2", "PvE", "Colors 5", "Comfort"};

    static void colorPage(Sink s, int start) {
        java.util.List<Feats.F> cl = Feats.colored();
        for (int i = start; i < Math.min(start + 14, cl.size()); i++) featColor(s, cl.get(i));
    }

    static void featToggle(Sink s, Feats.F f) {
        s.toggle(f.name(), () -> Config.on(f.key()), v -> Config.I.on.put(f.key(), v));
    }

    static void featColor(Sink s, Feats.F f) {
        s.palette("Color: " + f.name(), () -> Config.rgb(f.key(), f.def()), v -> Config.I.rgb.put(f.key(), v), () -> {});
    }

    public static void build(int page, Sink s) {
        Config c = Config.I;
        switch (page) {
            case 0 -> {
                s.toggle("Watermark", () -> c.watermark, v -> c.watermark = v);
                s.toggle("FPS", () -> c.fps, v -> c.fps = v);
                s.toggle("CPS", () -> c.cps, v -> c.cps = v);
                s.toggle("Ping", () -> c.ping, v -> c.ping = v);
                s.toggle("Coordinates", () -> c.coords, v -> c.coords = v);
                s.toggle("Keystrokes", () -> c.keystrokes, v -> c.keystrokes = v);
                s.toggle("Text shadow", () -> c.textShadow, v -> c.textShadow = v);
                s.slider("HUD scale", 0.5f, 2f, c.hudScale, false, v -> c.hudScale = v);
                s.toggle("Accent bar", () -> c.accentBar, v -> c.accentBar = v);
                s.toggle("Panel border", () -> c.hudBorder, v -> c.hudBorder = v);
                s.cycle("Text color", Config.TEXT_MODES, () -> c.textMode, v -> c.textMode = v);
                s.button("Open HUD editor", "Open HUD editor", () -> s.open(new HudEditorScreen(s.self())));
            }
            case 1 -> {
                s.toggle("Speed", () -> c.speed, v -> c.speed = v);
                s.toggle("Direction", () -> c.direction, v -> c.direction = v);
                s.toggle("Clock", () -> c.clock, v -> c.clock = v);
                s.toggle("RAM", () -> c.memory, v -> c.memory = v);
                s.toggle("Server IP", () -> c.server, v -> c.server = v);
                s.toggle("Combo counter", () -> c.combo, v -> c.combo = v);
                s.toggle("Armor HUD", () -> c.armorHud, v -> c.armorHud = v);
                s.toggle("Potion HUD", () -> c.potionHud, v -> c.potionHud = v);
                s.toggle("Target HUD", () -> c.targetHud, v -> c.targetHud = v);
                s.toggle("Move state", () -> c.state, v -> c.state = v);
                s.toggle("Totem/Gapple count", () -> c.itemCount, v -> c.itemCount = v);
            }
            case 2 -> {
                s.toggle("Custom crosshair", () -> c.crosshair, v -> c.crosshair = v);
                s.toggle("Cross", () -> c.chCross, v -> c.chCross = v);
                s.toggle("Center dot", () -> c.chDot, v -> c.chDot = v);
                s.toggle("Outline", () -> c.chOutline, v -> c.chOutline = v);
                s.toggle("Rainbow color", () -> c.chRainbow, v -> c.chRainbow = v);
                s.toggle("Dynamic gap", () -> c.chDynamic, v -> c.chDynamic = v);
                s.toggle("Circle", () -> c.chCircle, v -> c.chCircle = v);
                s.toggle("Custom color", () -> c.chCustom, v -> c.chCustom = v);
                s.palette("Crosshair palette", () -> c.chRgb, v -> c.chRgb = v, () -> c.chCustom = true);
                s.slider("Size", 1, 20, c.chSize, true, v -> c.chSize = Math.round(v));
                s.slider("Gap", 0, 15, c.chGap, true, v -> c.chGap = Math.round(v));
                s.slider("Thickness", 1, 6, c.chThick, true, v -> c.chThick = Math.round(v));
                s.toggle("Hitmarker", () -> c.hitmarker, v -> c.hitmarker = v);
                s.slider("Hitmarker size", 2, 12, c.hitSize, true, v -> c.hitSize = Math.round(v));
                s.slider("Hitmarker time", 2, 30, c.hitTicks, true, v -> c.hitTicks = Math.round(v));
            }
            case 3 -> {
                s.toggle("Trail", () -> c.trail, v -> c.trail = v);
                s.toggle("Trail in 1st person", () -> c.trailFirstPerson, v -> c.trailFirstPerson = v);
                s.slider("Trail density", 1, 8, c.trailDensity, true, v -> c.trailDensity = Math.round(v));
                s.toggle("China hat", () -> c.hat, v -> c.hat = v);
                s.slider("Hat size", 0.3f, 1.2f, c.hatSize, false, v -> c.hatSize = v);
                s.slider("Hat height", 0f, 0.8f, c.hatHeight, false, v -> c.hatHeight = v);
                s.slider("Hat points", 6, 30, c.hatPoints, true, v -> c.hatPoints = Math.round(v));
                s.toggle("Hit particles", () -> c.hitParticles, v -> c.hitParticles = v);
                s.slider("Particle count", 1, 30, c.hitCount, true, v -> c.hitCount = Math.round(v));
                s.cycle("Hit type", Extras.HIT_TYPES, () -> c.hitType, v -> c.hitType = v);
                s.cycle("Hit sound", Extras.SOUNDS, () -> c.hitSound, v -> c.hitSound = v);
                s.cycle("Particle color", Extras.COLORS, () -> c.pColor, v -> c.pColor = v);
                s.palette("Particle palette", () -> c.partRgb, v -> c.partRgb = v, () -> c.pColor = 3);
                s.toggle("No view bobbing", () -> c.noBob, v -> c.noBob = v);
                s.toggle("No sprint FOV", () -> c.noFovFx, v -> c.noFovFx = v);
                s.slider("Zoom FOV (key C)", 30, 70, c.zoomFov, true, v -> c.zoomFov = Math.round(v));
            }
            case 4 -> {
                s.toggle("Show in 1st person", () -> c.cosFirst, v -> c.cosFirst = v);
                s.toggle("Halo", () -> c.halo, v -> c.halo = v);
                s.slider("Halo size", 0.15f, 0.8f, c.haloSize, false, v -> c.haloSize = v);
                s.slider("Halo height", 0f, 0.8f, c.haloHeight, false, v -> c.haloHeight = v);
                s.toggle("Aura", () -> c.aura, v -> c.aura = v);
                s.slider("Aura orbs", 1, 16, c.auraCount, true, v -> c.auraCount = Math.round(v));
                s.slider("Aura radius", 0.4f, 1.6f, c.auraRadius, false, v -> c.auraRadius = v);
                s.slider("Aura speed", 0.2f, 4f, c.auraSpeed, false, v -> c.auraSpeed = v);
                s.toggle("Footsteps", () -> c.footsteps, v -> c.footsteps = v);
                s.toggle("Wings", () -> c.wings, v -> c.wings = v);
                s.slider("Wing size", 0.5f, 2f, c.wingSize, false, v -> c.wingSize = v);
                s.toggle("Kill effect", () -> c.killEffect, v -> c.killEffect = v);
            }
            case 5 -> {
                s.button("Preset: " + Config.PRESETS[c.preset], "Preset", () -> {
                    Config.applyPreset((c.preset + 1) % Config.PRESETS.length);
                    s.refresh();
                });
                s.slider("Accent hue", 0, 1, c.hue, false, v -> c.hue = v);
                s.toggle("Rainbow theme", () -> c.rainbow, v -> c.rainbow = v);
                s.slider("Rainbow speed", 0.1f, 5f, c.rainbowSpeed, false, v -> c.rainbowSpeed = v);
                s.slider("Panel opacity", 0, 255, c.bgOpacity, true, v -> c.bgOpacity = Math.round(v));
                s.toggle("Custom accent", () -> c.accentCustom, v -> c.accentCustom = v);
                s.palette("Accent palette", () -> c.accentRgb, v -> c.accentRgb = v, () -> c.accentCustom = true);
            }
            case 6 -> {
                s.toggle("Low HP warning", () -> c.lowHp, v -> c.lowHp = v);
                s.slider("Low HP at", 2, 18, c.lowHpThreshold, true, v -> c.lowHpThreshold = Math.round(v));
                s.toggle("Target HUD", () -> c.targetHud, v -> c.targetHud = v);
                s.toggle("Target armor", () -> c.targetArmor, v -> c.targetArmor = v);
                s.toggle("Distance + HP", () -> c.showReach, v -> c.showReach = v);
                s.toggle("Combat timer", () -> c.combatTimer, v -> c.combatTimer = v);
                s.slider("Combat seconds", 5, 60, c.combatSecs, true, v -> c.combatSecs = Math.round(v));
                s.toggle("Combo counter", () -> c.combo, v -> c.combo = v);
                s.toggle("Totem pops", () -> c.pops, v -> c.pops = v);
                s.toggle("Kill effect", () -> c.killEffect, v -> c.killEffect = v);
                s.toggle("Hitmarker", () -> c.hitmarker, v -> c.hitmarker = v);
                s.cycle("Hit sound", Extras.SOUNDS, () -> c.hitSound, v -> c.hitSound = v);
            }
            case 7 -> {
                s.toggle("Cooldown HUD", () -> c.cdHud, v -> c.cdHud = v);
                s.toggle("Ready ding", () -> c.cdReady, v -> c.cdReady = v);
                s.toggle("Item counts", () -> c.itemCount, v -> c.itemCount = v);
                s.toggle("Potion HUD", () -> c.potionHud, v -> c.potionHud = v);
                s.toggle("Armor HUD (red<15%)", () -> c.armorHud, v -> c.armorHud = v);
                s.toggle("Move state", () -> c.state, v -> c.state = v);
                s.toggle("Server IP", () -> c.server, v -> c.server = v);
                s.toggle("Clock", () -> c.clock, v -> c.clock = v);
                s.toggle("FPS guard", () -> c.perfGuard, v -> c.perfGuard = v);
                s.slider("FPS guard at", 20, 120, c.perfMinFps, true, v -> c.perfMinFps = Math.round(v));
            }
            case 8 -> {
                s.toggle("Day counter", () -> c.day, v -> c.day = v);
                s.toggle("World time", () -> c.worldTime, v -> c.worldTime = v);
                s.toggle("Biome", () -> c.biome, v -> c.biome = v);
                s.toggle("Light level", () -> c.light, v -> c.light = v);
                s.toggle("Yaw / Pitch", () -> c.angles, v -> c.angles = v);
                s.toggle("Held item", () -> c.heldItem, v -> c.heldItem = v);
                s.toggle("Nether coords", () -> c.netherCoords, v -> c.netherCoords = v);
                s.toggle("HP number", () -> c.hpText, v -> c.hpText = v);
                s.toggle("Food number", () -> c.hungerText, v -> c.hungerText = v);
                s.toggle("XP level", () -> c.xpLevel, v -> c.xpLevel = v);
                s.toggle("Session time", () -> c.session, v -> c.session = v);
                s.toggle("FPS color", () -> c.fpsColor, v -> c.fpsColor = v);
                s.toggle("Ping color", () -> c.pingColor, v -> c.pingColor = v);
                s.toggle("Mouse keys", () -> c.keysMouse, v -> c.keysMouse = v);
            }
            case 9 -> {
                s.cycle("Particle shape", Extras.SHAPES, () -> c.pShape, v -> c.pShape = v);
                s.toggle("Waist ring", () -> c.ring, v -> c.ring = v);
                s.slider("Ring radius", 0.3f, 1.5f, c.ringRadius, false, v -> c.ringRadius = v);
                s.toggle("Helix", () -> c.helix, v -> c.helix = v);
                s.toggle("Rain cloud", () -> c.cloud, v -> c.cloud = v);
                s.toggle("Jump rings", () -> c.jumpRings, v -> c.jumpRings = v);
                s.toggle("Level-up burst", () -> c.lvlBurst, v -> c.lvlBurst = v);
                s.toggle("Fireflies", () -> c.fireflies, v -> c.fireflies = v);
            }
            case 10 -> {
                s.toggle("Compass", () -> c.compass, v -> c.compass = v);
                s.toggle("Low food warning", () -> c.lowFood, v -> c.lowFood = v);
                s.toggle("Attack bar", () -> c.atkBar, v -> c.atkBar = v);
                s.toggle("Screen frame", () -> c.frame, v -> c.frame = v);
                s.slider("Frame thickness", 1, 10, c.frameThick, true, v -> c.frameThick = Math.round(v));
                s.toggle("Hurt flash", () -> c.hurtFlash, v -> c.hurtFlash = v);
                s.toggle("Kill counter", () -> c.kills, v -> c.kills = v);
                s.toggle("Best combo", () -> c.bestCombo, v -> c.bestCombo = v);
                s.toggle("Red on target", () -> c.chTargetColor, v -> c.chTargetColor = v);
                s.slider("Crosshair alpha", 30, 255, c.chAlpha, true, v -> c.chAlpha = Math.round(v));
            }
            case 11 -> {
                s.toggle("Snowfall", () -> c.snow, v -> c.snow = v);
                s.toggle("Sakura petals", () -> c.sakura, v -> c.sakura = v);
                s.toggle("Heart aura", () -> c.heartAura, v -> c.heartAura = v);
                s.toggle("Music notes", () -> c.notes, v -> c.notes = v);
                s.toggle("Electric aura", () -> c.electric, v -> c.electric = v);
                s.toggle("Flame feet", () -> c.flameFeet, v -> c.flameFeet = v);
                s.toggle("Soul orbs", () -> c.souls, v -> c.souls = v);
                s.toggle("Enchant glyphs", () -> c.enchant, v -> c.enchant = v);
                s.toggle("Totem aura", () -> c.totemAura, v -> c.totemAura = v);
                s.cycle("Kill effect type", Extras.KILL_TYPES, () -> c.killType, v -> c.killType = v);
                s.slider("Cosmetic intensity", 1, 5, c.cosIntensity, true, v -> c.cosIntensity = Math.round(v));
            }
            case 12 -> {
                s.slider("Cosmetic size", 0.5f, 2f, Config.num("cosScale", 1f), false, v -> Config.I.num.put("cosScale", v));
                s.slider("Cosmetic speed", 0.3f, 3f, Config.num("cosSpeed", 1f), false, v -> Config.I.num.put("cosSpeed", v));
                for (int i = 0; i < 12; i++) featToggle(s, Feats.COS[i]);
            }
            case 13 -> { for (int i = 12; i < Feats.COS.length; i++) featToggle(s, Feats.COS[i]); }
            case 14 -> { for (Feats.F f : Feats.HUD) featToggle(s, f); }
            case 15 -> {
                for (Feats.F f : Feats.SCR) featToggle(s, f);
                s.slider("Vignette strength", 0.1f, 1f, Config.num("vigAlpha", 0.5f), false, v -> Config.I.num.put("vigAlpha", v));
                s.slider("Letterbox height", 10, 120, Config.num("letterH", 40f), true, v -> Config.I.num.put("letterH", (float) Math.round(v)));
                s.slider("Tint alpha", 5, 150, Config.num("tintA", 40f), true, v -> Config.I.num.put("tintA", (float) Math.round(v)));
            }
            case 18 -> {
                s.slider("Cosmetic size", 0.5f, 2f, Config.num("cosScale", 1f), false, v -> Config.I.num.put("cosScale", v));
                s.slider("Cosmetic speed", 0.3f, 3f, Config.num("cosSpeed", 1f), false, v -> Config.I.num.put("cosSpeed", v));
                for (int i = 0; i < 13; i++) featToggle(s, Feats.COS2[i]);
            }
            case 19 -> { for (int i = 13; i < Feats.COS2.length; i++) featToggle(s, Feats.COS2[i]); }
            case 20 -> { for (Feats.F f : Feats.HUD2) featToggle(s, f); }
            case 21 -> {
                for (Feats.F f : Feats.SCR2) featToggle(s, f);
                s.slider("Scanline alpha", 10, 120, Config.num("scanA", 40f), true, v -> Config.I.num.put("scanA", (float) Math.round(v)));
                s.slider("Pillarbox width", 10, 120, Config.num("pillarW", 40f), true, v -> Config.I.num.put("pillarW", (float) Math.round(v)));
            }
            case 24 -> {
                s.slider("Armor alert at", 5, 40, Config.num("armorPct", 15f), true, v -> Config.I.num.put("armorPct", (float) Math.round(v)));
                s.slider("High ping at", 100, 500, Config.num("pingMs", 200f), true, v -> Config.I.num.put("pingMs", (float) Math.round(v)));
                for (Feats.F f : Feats.PVP) featToggle(s, f);
            }
            case 25 -> {
                s.slider("Tool warn at", 5, 30, Config.num("toolPct", 10f), true, v -> Config.I.num.put("toolPct", (float) Math.round(v)));
                for (Feats.F f : Feats.PVE) featToggle(s, f);
            }
            case 27 -> {
                s.toggle("Colorblind palette", () -> c.colorblind, v -> c.colorblind = v);
                s.toggle("Calm mode", () -> c.calm, v -> c.calm = v);
                s.toggle("High contrast", () -> c.highContrast, v -> c.highContrast = v);
                s.toggle("Reduced motion", () -> c.reducedMotion, v -> c.reducedMotion = v);
                s.toggle("Streamer mode", () -> c.streamer, v -> c.streamer = v);
                s.toggle("Quiet mode", () -> c.quiet, v -> c.quiet = v);
                s.toggle("Compact HUD", () -> c.compact, v -> c.compact = v);
                s.toggle("Welcome hint", () -> c.welcomeHint, v -> c.welcomeHint = v);
                s.toggle("Hide HUD in chat", () -> c.hideInChat, v -> c.hideInChat = v);
                s.toggle("Keep HUD on screen", () -> c.keepOnScreen, v -> c.keepOnScreen = v);
                s.toggle("Auto-shrink HUD", () -> c.autoShrink, v -> c.autoShrink = v);
                s.button("Apply comfort preset", "Apply comfort preset", () -> { Config.applyComfort(); s.refresh(); });
            }
            case 16 -> colorPage(s, 0);
            case 17 -> colorPage(s, 14);
            case 22 -> colorPage(s, 28);
            case 23 -> colorPage(s, 42);
            default -> colorPage(s, 56);
        }

    }

    private Pages() {}
}
