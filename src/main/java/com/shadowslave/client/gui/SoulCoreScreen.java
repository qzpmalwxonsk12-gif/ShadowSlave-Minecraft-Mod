package com.shadowslave.client.gui;

import com.shadowslave.client.ClientSoulData;
import com.shadowslave.soul.Rank;
import com.shadowslave.soul.SoulData;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

/**
 * The dedicated Soul Core interface.
 *
 * Drawn entirely with primitives (no texture assets) so it is fully self-contained:
 *  - An animated, liquid-filling Soul Core orb whose level reflects stored Essence.
 *  - The Aether bar (naturally regenerating skill energy).
 *  - The current Rank with its health/damage multiplier.
 *  - Flaw and Aspect placeholders.
 */
public class SoulCoreScreen extends Screen {

    // Panel geometry
    private static final int PANEL_W = 288;
    private static final int PANEL_H = 196;

    // Palette (ARGB)
    private static final int COL_PANEL_TOP = 0xF21A1426;
    private static final int COL_PANEL_BOTTOM = 0xF20D0A14;
    private static final int COL_BORDER = 0xFF4A3D6B;
    private static final int COL_BORDER_INNER = 0xFF2A2440;
    private static final int COL_TITLE = 0xFFE8E2FF;
    private static final int COL_LABEL = 0xFF9A8FC4;
    private static final int COL_VALUE = 0xFFE8E2FF;
    private static final int COL_MUTED = 0xFF6E6790;
    private static final int COL_DIVIDER = 0xFF2A2440;

    private static final int COL_ORB_EMPTY = 0xFF0E1622;
    private static final int COL_ORB_FILL = 0xFF2E9BE0;
    private static final int COL_ORB_SURFACE = 0xFF9BE6FF;

    private static final int COL_AETHER = 0xFF45C7F0;
    private static final int COL_AETHER_BG = 0xFF14202E;

    private long openTime;

    public SoulCoreScreen() {
        super(Component.literal("Soul Core"));
    }

    @Override
    protected void init() {
        super.init();
        this.openTime = System.currentTimeMillis();
    }

    @Override
    public void render(GuiGraphics gg, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(gg);

        SoulData data = ClientSoulData.get();
        Rank rank = data.getRank();

        int left = (this.width - PANEL_W) / 2;
        int top = (this.height - PANEL_H) / 2;
        long time = System.currentTimeMillis() - openTime;

        // ---- Panel ----
        gg.fillGradient(left, top, left + PANEL_W, top + PANEL_H, COL_PANEL_TOP, COL_PANEL_BOTTOM);
        drawBorder(gg, left, top, PANEL_W, PANEL_H, COL_BORDER, COL_BORDER_INNER);

        // ---- Title ----
        gg.drawCenteredString(this.font, Component.literal("◈ S O U L   C O R E ◈"),
                left + PANEL_W / 2, top + 9, COL_TITLE);
        gg.fill(left + 24, top + 22, left + PANEL_W - 24, top + 23, COL_DIVIDER);

        // ---- Soul Core orb (left) ----
        int orbCx = left + 76;
        int orbCy = top + 110;
        int orbR = 56;
        double fill = Mth.clamp(data.getCoreFillPercent() / 100.0, 0.0, 1.0);
        drawSoulOrb(gg, orbCx, orbCy, orbR, fill, rank.getColor(), time);

        // Percentage in the orb centre
        String pct = String.format("%.1f%%", data.getCoreFillPercent());
        gg.drawCenteredString(this.font, pct, orbCx, orbCy - 4, 0xFFFFFFFF);
        gg.drawCenteredString(this.font, Component.literal("ESSENCE"), orbCx, orbCy + 7, 0xFFB9D9F0);

        // Vertical divider between orb and stats
        int divX = left + 152;
        gg.fill(divX, top + 30, divX + 1, top + PANEL_H - 12, COL_DIVIDER);

        // ---- Right column ----
        int rx = divX + 14;
        int rw = left + PANEL_W - 16 - rx;
        int ry = top + 34;

        // Rank
        gg.drawString(this.font, "RANK", rx, ry, COL_LABEL, false);
        gg.drawString(this.font, Component.literal(rank.getDisplayName().toUpperCase()), rx, ry + 11, rank.getColor(), false);
        gg.drawString(this.font, String.format("×%.2f Health & Damage", rank.getMultiplier()),
                rx, ry + 23, COL_MUTED, false);
        ry += 40;

        // Aether bar
        gg.drawString(this.font, "AETHER", rx, ry, COL_LABEL, false);
        String aetherVal = String.format("%d / %d", Math.round(data.getAether()), Math.round(data.getMaxAether()));
        int aetherValW = this.font.width(aetherVal);
        gg.drawString(this.font, aetherVal, rx + rw - aetherValW, ry, COL_VALUE, false);
        drawBar(gg, rx, ry + 11, rw, 7, data.getAetherFraction(), COL_AETHER, COL_AETHER_BG);
        gg.drawString(this.font, "Regenerates over time · powers skills", rx, ry + 21, COL_MUTED, false);
        ry += 38;

        // Soul Core capacity / upgrades
        gg.drawString(this.font, "SOUL CORE", rx, ry, COL_LABEL, false);
        String cap = String.format("cap ×%d", 1 + data.getCoreUpgrades());
        int capW = this.font.width(cap);
        gg.drawString(this.font, cap, rx + rw - capW, ry, COL_VALUE, false);
        gg.drawString(this.font, String.format("Nether Stars absorbed: %d", data.getCoreUpgrades()),
                rx, ry + 11, COL_MUTED, false);
        if (Math.abs(data.getEssenceMultiplier() - 1.0) > 0.001) {
            gg.drawString(this.font, String.format("Essence gain ×%.2f", data.getEssenceMultiplier()),
                    rx, ry + 21, 0xFF8FE38F, false);
            ry += 32;
        } else {
            ry += 28;
        }

        // Flaw & Aspect (placeholders)
        gg.drawString(this.font, "FLAW", rx, ry, COL_LABEL, false);
        gg.drawString(this.font, Component.literal("[ " + data.getFlaw() + " ]"), rx + 34, ry, COL_MUTED, false);
        ry += 12;
        gg.drawString(this.font, "ASPECT", rx, ry, COL_LABEL, false);
        gg.drawString(this.font, Component.literal("[ " + data.getAspect() + " ]"), rx + 44, ry, COL_MUTED, false);

        // ---- Footer hint ----
        gg.drawCenteredString(this.font, Component.literal("Kills feed your Soul Core  ·  Nether Star expands it"),
                left + PANEL_W / 2, top + PANEL_H - 12, COL_MUTED);

        super.render(gg, mouseX, mouseY, partialTick);
    }

    /* ------------------------------------------------------------------ */
    /* Drawing helpers                                                     */
    /* ------------------------------------------------------------------ */

    private static void drawBorder(GuiGraphics gg, int x, int y, int w, int h, int outer, int inner) {
        // outer frame
        gg.fill(x - 1, y - 1, x + w + 1, y, outer);
        gg.fill(x - 1, y + h, x + w + 1, y + h + 1, outer);
        gg.fill(x - 1, y, x, y + h, outer);
        gg.fill(x + w, y, x + w + 1, y + h, outer);
        // inner frame
        gg.fill(x + 2, y + 2, x + w - 2, y + 3, inner);
        gg.fill(x + 2, y + h - 3, x + w - 2, y + h - 2, inner);
        gg.fill(x + 2, y + 2, x + 3, y + h - 2, inner);
        gg.fill(x + w - 3, y + 2, x + w - 2, y + h - 2, inner);
    }

    private static void drawBar(GuiGraphics gg, int x, int y, int w, int h, double frac, int fg, int bg) {
        frac = Mth.clamp(frac, 0.0, 1.0);
        gg.fill(x - 1, y - 1, x + w + 1, y + h + 1, 0xFF000000);
        gg.fill(x, y, x + w, y + h, bg);
        int filled = (int) Math.round(w * frac);
        if (filled > 0) {
            gg.fill(x, y, x + filled, y + h, fg);
            // subtle top highlight line
            gg.fill(x, y, x + filled, y + 1, 0x66FFFFFF | (fg & 0x00FFFFFF));
        }
    }

    /** Solid filled circle, drawn column by column (pixel aesthetic, no AA). */
    private static void fillCircle(GuiGraphics gg, int cx, int cy, int r, int color) {
        for (int x = -r; x <= r; x++) {
            int dy = (int) Math.sqrt((double) r * r - (double) x * x);
            gg.fill(cx + x, cy - dy, cx + x + 1, cy + dy + 1, color);
        }
    }

    /**
     * The Soul Core orb: a coloured rank ring, a dark interior, and an animated liquid
     * fill that rises with the stored Essence.
     */
    private static void drawSoulOrb(GuiGraphics gg, int cx, int cy, int r, double fillFrac, int ringColor, long time) {
        // Rank-coloured ring + dark interior
        fillCircle(gg, cx, cy, r + 3, ringColor);
        fillCircle(gg, cx, cy, r, COL_ORB_EMPTY);

        // Liquid fill, column by column, with a gentle surface wave
        double baseSurface = (cy + r) - 2.0 * r * fillFrac;
        for (int x = -r; x <= r; x++) {
            int dy = (int) Math.sqrt((double) r * r - (double) x * x);
            int colTop = cy - dy;
            int colBottom = cy + dy;

            double wave = Math.sin(time / 320.0 + x * 0.18) * 1.8;
            int surface = (int) Math.round(baseSurface + wave);
            int fillTop = Math.max(colTop, surface);

            if (fillTop < colBottom) {
                gg.fill(cx + x, fillTop, cx + x + 1, colBottom + 1, COL_ORB_FILL);
                // bright surface crest, only where liquid isn't clipped by the orb top
                if (fillFrac < 0.999 && surface >= colTop) {
                    gg.fill(cx + x, fillTop, cx + x + 1, fillTop + 2, COL_ORB_SURFACE);
                }
            }
        }

        // Glossy highlight in the upper-left
        gg.fill(cx - r / 2, cy - r / 2, cx - r / 2 + 6, cy - r / 2 + 6, 0x33FFFFFF);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
