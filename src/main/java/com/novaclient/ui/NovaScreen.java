package com.novaclient.ui;

import com.novaclient.compat.Compatibility;
import com.novaclient.module.ModuleCategory;
import com.novaclient.module.ModuleManager;
import com.novaclient.module.NovaModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Nova's in-game console. It deliberately uses Minecraft primitives rather than a webview,
 * so it stays lightweight and plays well with Sodium, Iris, and other Fabric clients.
 */
public final class NovaScreen extends Screen {
    private static final int BG = 0xFF080C11;
    private static final int PANEL = 0xFF10171E;
    private static final int PANEL_ALT = 0xFF0D141A;
    private static final int BORDER = 0xFF202B32;
    private static final int TEXT = 0xFFEAF2EF;
    private static final int MUTED = 0xFF829097;
    private static final int DIM = 0xFF526169;
    private static final int MINT = 0xFF85F2CB;
    private static final int MINT_DARK = 0xFF205548;
    private static final int AMBER = 0xFFF0C779;
    private static final int RED = 0xFFFF817F;

    private final Screen parent;
    private Tab tab = Tab.DASHBOARD;
    private ModuleCategory categoryFilter;
    private String query = "";
    private int moduleScroll;
    private TextFieldWidget searchField;

    public NovaScreen(Screen parent) {
        super(Text.literal("NovaClient"));
        this.parent = parent;
    }

    private enum Tab {
        DASHBOARD("Overview", "01"),
        MODULES("Modules", "02"),
        PRESETS("Profiles", "03"),
        SETTINGS("Settings", "04");

        final String label;
        final String number;

        Tab(String label, String number) {
            this.label = label;
            this.number = number;
        }
    }

    @Override
    protected void init() {
        if (tab == Tab.MODULES) {
            searchField = new TextFieldWidget(textRenderer, width - 205, 72, 175, 22, Text.literal("Search modules"));
            searchField.setMaxLength(40);
            searchField.setPlaceholder(Text.literal("Search modules"));
            searchField.setChangedListener(value -> {
                query = value;
                moduleScroll = 0;
            });
            addDrawableChild(searchField);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, BG);
        drawGrid(context);
        drawSidebar(context, mouseX, mouseY);
        drawTopbar(context);
        switch (tab) {
            case DASHBOARD -> drawDashboard(context, mouseX, mouseY);
            case MODULES -> drawModules(context, mouseX, mouseY);
            case PRESETS -> drawPresets(context, mouseX, mouseY);
            case SETTINGS -> drawSettings(context, mouseX, mouseY);
        }
        super.render(context, mouseX, mouseY, delta);
    }

    private void drawGrid(DrawContext context) {
        for (int x = 230; x < width; x += 32) context.fill(x, 0, x + 1, height, 0x120F1B22);
        for (int y = 0; y < height; y += 32) context.fill(230, y, width, y + 1, 0x120F1B22);
    }

    private void drawSidebar(DrawContext context, int mouseX, int mouseY) {
        context.fill(0, 0, 208, height, 0xFF0B1117);
        context.fill(207, 0, 208, height, BORDER);

        // Compact geometric N mark.
        context.fill(24, 25, 29, 54, MINT);
        context.fill(29, 25, 33, 29, MINT);
        context.fill(33, 29, 37, 33, MINT);
        context.fill(37, 33, 41, 37, MINT);
        context.fill(41, 37, 45, 41, MINT);
        context.fill(45, 41, 49, 45, MINT);
        context.fill(49, 45, 53, 54, MINT);
        text(context, "NOVA", 63, 27, TEXT);
        text(context, "CLIENT / 1.0.0", 63, 42, MUTED);

        text(context, "CONTROL CENTER", 23, 91, DIM);
        int navY = 112;
        for (Tab item : Tab.values()) {
            boolean active = item == tab;
            int y = navY + item.ordinal() * 43;
            if (active) {
                context.fill(17, y, 191, y + 34, 0xFF172A2B);
                context.fill(17, y, 20, y + 34, MINT);
            }
            text(context, item.number, 28, y + 10, active ? MINT : DIM);
            text(context, item.label, 62, y + 10, active ? TEXT : MUTED);
        }

        int bottom = height - 64;
        context.fill(17, bottom, 191, bottom + 1, BORDER);
        context.fill(27, bottom + 18, 33, bottom + 24, MINT);
        text(context, "OPTIMIZER ONLINE", 44, bottom + 16, TEXT);
        text(context, Compatibility.rendererLabel(), 44, bottom + 31, MUTED);
    }

    private void drawTopbar(DrawContext context) {
        text(context, "NOVA / " + tab.label.toUpperCase(Locale.ROOT), 232, 27, MUTED);
        text(context, "RIGHT SHIFT", width - 111, 27, DIM);
        context.fill(width - 123, 25, width - 116, 32, MINT);
        context.fill(232, 49, width - 25, 50, BORDER);
    }

    private void drawDashboard(DrawContext context, int mouseX, int mouseY) {
        int left = 232;
        int right = width - 25;
        int contentWidth = right - left;

        panel(context, left, 74, right, 211, PANEL);
        text(context, "PERFORMANCE CONSOLE", left + 20, 94, MINT);
        text(context, "A quiet client with a strict frame budget.", left + 20, 113, MUTED);
        text(context, ModuleManager.rendererStatus(), left + 20, 138, TEXT);
        text(context, "Sodium stays in control of rendering when installed.", left + 20, 155, DIM);
        chip(context, left + 20, 177, "FABRIC ONLY", MINT_DARK, MINT);
        chip(context, left + 110, 177, "1.21.1", 0xFF1A222A, MUTED);

        // FPS ring / readout.
        int cx = right - 95;
        int cy = 143;
        context.fill(cx - 39, cy - 39, cx + 39, cy - 37, BORDER);
        context.fill(cx - 39, cy + 37, cx + 39, cy + 39, BORDER);
        context.fill(cx - 39, cy - 37, cx - 37, cy + 37, BORDER);
        context.fill(cx + 37, cy - 37, cx + 39, cy + 37, MINT_DARK);
        context.fill(cx - 39, cy - 39, cx - 5, cy - 37, MINT);
        centered(context, "240", cx, cy - 8, TEXT);
        centered(context, "FPS / TARGET", cx, cy + 15, MUTED);

        statCard(context, left, 300, 178, "ACTIVE MODULES", ModuleManager.enabledCount() + " / " + ModuleManager.totalCount(), "stable load", MINT);
        statCard(context, left + 190, 300, 178, "FRAME TIME", "4.16 ms", "excellent", MINT);
        statCard(context, left + 380, 300, contentWidth - 380, "RUNTIME", "LOCAL ONLY", "telemetry shielded", AMBER);

        panel(context, left, 391, right, Math.min(height - 20, 520), PANEL_ALT);
        text(context, "QUICK SWITCHES", left + 20, 412, TEXT);
        text(context, "High-impact modules. Full catalogue in Modules.", left + 20, 430, MUTED);
        quickModule(context, left + 20, 448, "Entity Culling", "entity_culling", mouseX, mouseY);
        quickModule(context, left + 210, 448, "Particle Optimizer", "particle_optimizer", mouseX, mouseY);
        quickModule(context, left + 400, 448, "Dynamic FPS", "dynamic_fps", mouseX, mouseY);
    }

    private void drawModules(DrawContext context, int mouseX, int mouseY) {
        int left = 232;
        int right = width - 25;
        text(context, "MODULE LIBRARY", left, 77, TEXT);
        text(context, ModuleManager.totalCount() + " tuned switches / no server hooks", left, 94, MUTED);

        int filterX = left;
        chip(context, filterX, 104, "ALL", categoryFilter == null ? MINT_DARK : PANEL, categoryFilter == null ? MINT : MUTED);
        filterX += 46;
        for (ModuleCategory category : ModuleCategory.values()) {
            int chipWidth = category == ModuleCategory.PERFORMANCE ? 91 : category == ModuleCategory.RENDER ? 64 : category == ModuleCategory.WORLD ? 62 : 72;
            chip(context, filterX, 104, category.label().toUpperCase(Locale.ROOT), categoryFilter == category ? MINT_DARK : PANEL, categoryFilter == category ? MINT : MUTED);
            filterX += chipWidth + 6;
        }

        List<NovaModule> visible = filteredModules();
        int top = 145 - moduleScroll;
        int cardW = (right - left - 12) / 2;
        for (int i = 0; i < visible.size(); i++) {
            NovaModule module = visible.get(i);
            int col = i % 2;
            int row = i / 2;
            int x = left + col * (cardW + 12);
            int y = top + row * 70;
            if (y < 135 || y > height - 16) continue;
            drawModuleCard(context, module, x, y, cardW, mouseX, mouseY);
        }
        if (visible.isEmpty()) {
            text(context, "No modules match this search.", left + 20, 166, MUTED);
        }
        int maxScroll = Math.max(0, ((visible.size() + 1) / 2) * 70 - (height - 160));
        if (maxScroll > 0) {
            int trackX = right + 7;
            context.fill(trackX, 145, trackX + 2, height - 16, BORDER);
            int thumbH = Math.max(26, (height - 160) * (height - 160) / (((visible.size() + 1) / 2) * 70));
            int thumbY = 145 + (height - 175 - thumbH) * moduleScroll / maxScroll;
            context.fill(trackX, thumbY, trackX + 2, thumbY + thumbH, MINT);
        }
    }

    private List<NovaModule> filteredModules() {
        List<NovaModule> result = new ArrayList<>();
        String normalized = query.trim().toLowerCase(Locale.ROOT);
        for (NovaModule module : ModuleManager.all()) {
            if (categoryFilter != null && module.category() != categoryFilter) continue;
            if (!normalized.isEmpty() && !module.name().toLowerCase(Locale.ROOT).contains(normalized)
                    && !module.description().toLowerCase(Locale.ROOT).contains(normalized)) continue;
            result.add(module);
        }
        return result;
    }

    private void drawModuleCard(DrawContext context, NovaModule module, int x, int y, int w, int mouseX, int mouseY) {
        boolean hovered = mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + 61;
        int bg = hovered ? 0xFF162128 : PANEL;
        panel(context, x, y, x + w, y + 61, bg);
        context.fill(x, y, x + 3, y + 61, module.enabled() ? MINT : BORDER);
        text(context, module.name(), x + 14, y + 11, module.enabled() ? TEXT : MUTED);
        String description = module.description();
        if (description.length() > 42) description = description.substring(0, 40) + "…";
        text(context, description, x + 14, y + 29, DIM);
        text(context, module.category().label().toUpperCase(Locale.ROOT), x + 14, y + 46, module.enabled() ? MINT : DIM);
        toggle(context, x + w - 40, y + 16, module.enabled());
    }

    private void drawPresets(DrawContext context, int mouseX, int mouseY) {
        int left = 232;
        text(context, "PERFORMANCE PROFILES", left, 78, TEXT);
        text(context, "One click to tune the whole stack. Every choice is reversible.", left, 96, MUTED);
        int y = 124;
        presetCard(context, left, y, 174, "COMPETITIVE", "Lowest latency", "clean / sharp / fast", "competitive", MINT, mouseX, mouseY);
        presetCard(context, left + 187, y, 174, "BALANCED", "The daily driver", "stable / efficient", "balanced", MINT, mouseX, mouseY);
        presetCard(context, left + 374, y, width - 25 - (left + 374), "CINEMATIC", "Keep the details", "rich / smooth / calm", "cinematic", AMBER, mouseX, mouseY);

        panel(context, left, 302, width - 25, 404, PANEL_ALT);
        text(context, "PROFILE NOTES", left + 20, 324, TEXT);
        note(context, "Competitive", "Render and performance modules lead. Cosmetic blur and heavy world work stay off.", 352, MINT);
        note(context, "Balanced", "The default Nova shape: safe client-side optimizations, no forced distance caps.", 377, MINT);
        note(context, "Cinematic", "Preserves shadows, particles, and cloud detail for screenshots and recording.", 402, AMBER);
    }

    private void drawSettings(DrawContext context, int mouseX, int mouseY) {
        int left = 232;
        int right = width - 25;
        text(context, "CLIENT SETTINGS", left, 78, TEXT);
        text(context, "Small controls. No accounts, no telemetry, no network requests.", left, 96, MUTED);

        panel(context, left, 123, right, 194, PANEL);
        settingRow(context, left + 20, 139, "Telemetry shield", "Nova never sends gameplay data.", true, true);
        settingRow(context, left + 20, 179, "In-game HUD", "Show FPS and enabled module count.", ModuleManager.isShowHud(), true);

        panel(context, left, 211, right, 310, PANEL);
        text(context, "COMPATIBILITY", left + 20, 231, MINT);
        compatibilityLine(context, left + 20, 255, "Minecraft", "1.21.1", true);
        compatibilityLine(context, left + 20, 278, "Loader", "Fabric 0.16.10+", true);
        compatibilityLine(context, left + 20, 301, "Renderer", Compatibility.rendererLabel(), true);
        text(context, "Sodium " + Compatibility.sodiumVersion(), left + 225, 301, MUTED);

        panel(context, left, 327, right, 420, PANEL_ALT);
        text(context, "KEYBINDS", left + 20, 348, TEXT);
        text(context, "Open NovaClient", left + 20, 371, MUTED);
        chip(context, left + 184, 364, "RIGHT SHIFT", 0xFF1A222A, TEXT);
        text(context, "All state is stored in config/novaclient.properties.", left + 20, 399, DIM);
    }

    private void statCard(DrawContext context, int x, int y, int w, String eyebrow, String value, String caption, int accent) {
        panel(context, x, y, x + w, y + 72, PANEL);
        text(context, eyebrow, x + 14, y + 14, DIM);
        text(context, value, x + 14, y + 33, TEXT);
        text(context, caption, x + 14, y + 54, accent);
    }

    private void quickModule(DrawContext context, int x, int y, String label, String id, int mouseX, int mouseY) {
        boolean on = ModuleManager.enabled(id);
        boolean hovered = mouseX >= x && mouseX <= x + 166 && mouseY >= y && mouseY <= y + 26;
        if (hovered) context.fill(x, y, x + 166, y + 26, 0xFF18242A);
        context.fill(x, y + 4, x + 5, y + 9, on ? MINT : BORDER);
        text(context, label, x + 13, y + 1, on ? TEXT : MUTED);
        text(context, on ? "ON" : "OFF", x + 130, y + 1, on ? MINT : DIM);
    }

    private void presetCard(DrawContext context, int x, int y, int w, String title, String subtitle, String detail, String preset, int accent, int mouseX, int mouseY) {
        boolean hovered = mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + 146;
        panel(context, x, y, x + w, y + 146, hovered ? 0xFF162128 : PANEL);
        context.fill(x, y, x + 3, y + 146, accent);
        text(context, title, x + 16, y + 20, accent);
        text(context, subtitle, x + 16, y + 53, TEXT);
        text(context, detail, x + 16, y + 72, MUTED);
        context.fill(x + 16, y + 105, x + w - 16, y + 106, BORDER);
        text(context, "APPLY PROFILE", x + 16, y + 117, hovered ? TEXT : DIM);
    }

    private void note(DrawContext context, String title, String body, int y, int accent) {
        context.fill(252, y + 4, 257, y + 9, accent);
        text(context, title, 266, y, TEXT);
        text(context, body, 348, y, MUTED);
    }

    private void settingRow(DrawContext context, int x, int y, String title, String subtitle, boolean value, boolean locked) {
        text(context, title, x, y, TEXT);
        text(context, subtitle, x, y + 16, MUTED);
        if (locked) chip(context, width - 114, y - 5, "LOCAL", 0xFF1A222A, MUTED);
        else toggle(context, width - 58, y - 3, value);
    }

    private void compatibilityLine(DrawContext context, int x, int y, String label, String value, boolean okay) {
        text(context, label, x, y, MUTED);
        text(context, value, x + 104, y, TEXT);
        context.fill(x + 210, y + 4, x + 216, y + 10, okay ? MINT : RED);
    }

    private void panel(DrawContext context, int x1, int y1, int x2, int y2, int color) {
        if (x2 <= x1 || y2 <= y1) return;
        context.fill(x1, y1, x2, y2, color);
        context.fill(x1, y1, x2, y1 + 1, BORDER);
        context.fill(x1, y2 - 1, x2, y2, BORDER);
        context.fill(x1, y1, x1 + 1, y2, BORDER);
        context.fill(x2 - 1, y1, x2, y2, BORDER);
    }

    private void chip(DrawContext context, int x, int y, String label, int background, int foreground) {
        int w = textRenderer.getWidth(Text.literal(label)) + 18;
        context.fill(x, y, x + w, y + 20, background);
        context.fill(x, y, x + 2, y + 20, foreground);
        text(context, label, x + 9, y + 6, foreground);
    }

    private void toggle(DrawContext context, int x, int y, boolean enabled) {
        context.fill(x, y, x + 28, y + 14, enabled ? MINT_DARK : 0xFF202930);
        context.fill(x + (enabled ? 17 : 3), y + 3, x + (enabled ? 25 : 11), y + 11, enabled ? MINT : DIM);
    }

    private void text(DrawContext context, String value, int x, int y, int color) {
        context.drawTextWithShadow(textRenderer, Text.literal(value), x, y, color);
    }

    private void centered(DrawContext context, String value, int centerX, int y, int color) {
        int w = textRenderer.getWidth(Text.literal(value));
        text(context, value, centerX - w / 2, y, color);
    }

    private void switchTab(Tab next) {
        if (next == tab) return;
        NovaScreen screen = new NovaScreen(parent);
        screen.tab = next;
        screen.categoryFilter = categoryFilter;
        screen.query = query;
        MinecraftClient.getInstance().setScreen(screen);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) return true;
        if (button != 0) return false;

        if (mouseX < 208 && mouseY >= 112 && mouseY < 112 + Tab.values().length * 43) {
            int index = (int) ((mouseY - 112) / 43);
            switchTab(Tab.values()[index]);
            return true;
        }

        if (tab == Tab.DASHBOARD && mouseY >= 440 && mouseY <= 482) {
            int relative = (int) mouseX - 252;
            if (relative >= 0 && relative < 166) ModuleManager.toggle("entity_culling", client);
            else if (relative >= 190 && relative < 356) ModuleManager.toggle("particle_optimizer", client);
            else if (relative >= 380 && relative < 546) ModuleManager.toggle("dynamic_fps", client);
            return true;
        }

        if (tab == Tab.MODULES) {
            if (mouseY >= 104 && mouseY <= 124) {
                if (mouseX < 278) categoryFilter = null;
                else if (mouseX < 369) categoryFilter = ModuleCategory.PERFORMANCE;
                else if (mouseX < 439) categoryFilter = ModuleCategory.RENDER;
                else if (mouseX < 507) categoryFilter = ModuleCategory.WORLD;
                else categoryFilter = ModuleCategory.SYSTEM;
                moduleScroll = 0;
                return true;
            }
            List<NovaModule> visible = filteredModules();
            int left = 232;
            int right = width - 25;
            int cardW = (right - left - 12) / 2;
            int row = (int) ((mouseY - (145 - moduleScroll)) / 70);
            int col = mouseX < left + cardW + 6 ? 0 : 1;
            int index = row * 2 + col;
            if (row >= 0 && index >= 0 && index < visible.size()) {
                int x = left + col * (cardW + 12);
                if (mouseX >= x && mouseX <= x + cardW) {
                    ModuleManager.toggle(visible.get(index).id(), client);
                    return true;
                }
            }
        }

        if (tab == Tab.PRESETS && mouseY >= 124 && mouseY <= 270) {
            int left = 232;
            int card = mouseX < left + 174 ? 0 : mouseX < left + 361 ? 1 : 2;
            ModuleManager.applyPreset(card == 0 ? "competitive" : card == 1 ? "balanced" : "cinematic", client);
            return true;
        }

        if (tab == Tab.SETTINGS && mouseY >= 179 && mouseY <= 209) {
            ModuleManager.setShowHud(!ModuleManager.isShowHud());
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (tab == Tab.MODULES) {
            int max = Math.max(0, ((filteredModules().size() + 1) / 2) * 70 - (height - 160));
            moduleScroll = (int) Math.max(0, Math.min(max, moduleScroll - verticalAmount * 70));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void close() {
        MinecraftClient.getInstance().setScreen(parent);
    }
}
