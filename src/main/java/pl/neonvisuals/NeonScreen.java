package pl.neonvisuals;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

/** Click GUI: kolumny z modułami (Combat, World, HUD), panel Info i panel motywów. */
public class NeonScreen extends Screen {
    private static final int PW = 112;
    private static final int GAP = 6;
    private static final int ROW = 18;
    private static final int HEAD = 22;

    private static final Feature.Category[] CATS = {Feature.Category.COMBAT, Feature.Category.WORLD, Feature.Category.HUD};

    private final int[] px = new int[4];
    private int py;
    private final List<ButtonWidget> rowBtns = new ArrayList<>();
    private final List<Feature> rowFeats = new ArrayList<>();
    private final List<ButtonWidget> themeBtns = new ArrayList<>();

    public NeonScreen() {
        super(Text.literal(Brand.NAME));
    }

    private static List<Feature> of(Feature.Category c) {
        List<Feature> out = new ArrayList<>();
        for (Feature f : Feature.values()) {
            if (f.category == c) out.add(f);
        }
        return out;
    }

    private static int panelH(int rows) {
        return HEAD + rows * ROW + 4;
    }

    @Override
    protected void init() {
        rowBtns.clear();
        rowFeats.clear();
        themeBtns.clear();

        int left = (width - (4 * PW + 3 * GAP)) / 2;
        py = Math.max(10, (height - 150) / 2);
        for (int i = 0; i < 4; i++) {
            px[i] = left + i * (PW + GAP);
        }

        for (int c = 0; c < CATS.length; c++) {
            List<Feature> fs = of(CATS[c]);
            for (int k = 0; k < fs.size(); k++) {
                final Feature f = fs.get(k);
                ButtonWidget b = ButtonWidget.builder(Text.empty(), btn -> f.toggle())
                        .dimensions(px[c] + 4, py + HEAD + k * ROW, PW - 8, ROW - 2).build();
                addDrawableChild(b);
                rowBtns.add(b);
                rowFeats.add(f);
            }
        }

        for (int t = 0; t < NeonConfig.THEME_NAMES.length; t++) {
            final int idx = t;
            ButtonWidget b = ButtonWidget.builder(Text.empty(), btn -> NeonConfig.theme = idx)
                    .dimensions(px[3] + 4, py + HEAD + t * ROW, PW - 8, ROW - 2).build();
            addDrawableChild(b);
            themeBtns.add(b);
        }
    }

    // ---------- pomocnicze ----------

    private static void box(DrawContext ctx, int x1, int y1, int x2, int y2, int color) {
        ctx.fill(x1 + 1, y1, x2 - 1, y2, color);
        ctx.fill(x1, y1 + 1, x2, y2 - 1, color);
    }

    private static void frame(DrawContext ctx, int x1, int y1, int x2, int y2, int color) {
        ctx.fill(x1 + 1, y1, x2 - 1, y1 + 1, color);
        ctx.fill(x1 + 1, y2 - 1, x2 - 1, y2, color);
        ctx.fill(x1, y1 + 1, x1 + 1, y2 - 1, color);
        ctx.fill(x2 - 1, y1 + 1, x2, y2 - 1, color);
    }

    private static boolean hover(ButtonWidget b, int mx, int my) {
        return mx >= b.getX() && mx < b.getX() + b.getWidth() && my >= b.getY() && my < b.getY() + b.getHeight();
    }

    private void panel(DrawContext ctx, int x, int y, int h, String title) {
        box(ctx, x, y, x + PW, y + h, 0xE0121420);
        frame(ctx, x, y, x + PW, y + h, 0xFF2A2F48);
        ctx.drawTextWithShadow(textRenderer, Text.literal(title).formatted(Formatting.BOLD), x + 8, y + 7, 0xFFFFFFFF);
        ctx.fillGradient(x + 6, y + 18, x + PW - 6, y + 19, Neon.argb(0f), Neon.argb(0.3f));
    }

    // ---------- rysowanie ----------

    @Override
    public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.renderBackground(ctx, mouseX, mouseY, delta);

        for (int c = 0; c < CATS.length; c++) {
            panel(ctx, px[c], py, panelH(of(CATS[c]).size()), CATS[c].title);
        }

        // Info pod panelem HUD
        int infoY = py + panelH(of(Feature.Category.HUD).size()) + GAP;
        panel(ctx, px[2], infoY, 62, "Info");
        ctx.drawTextWithShadow(textRenderer, Neon.rainbowText(Brand.NAME), px[2] + 8, infoY + 26, 0xFFFFFFFF);
        ctx.drawTextWithShadow(textRenderer, Text.literal("by " + Brand.AUTHOR), px[2] + 8, infoY + 38, 0xFF8A90A5);
        ctx.drawTextWithShadow(textRenderer, Text.literal("Menu: klawisz ="), px[2] + 8, infoY + 49, 0xFF6F778A);

        panel(ctx, px[3], py, panelH(NeonConfig.THEME_NAMES.length), "Themes");
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);

        // wiersze modułów
        for (int i = 0; i < rowBtns.size(); i++) {
            ButtonWidget b = rowBtns.get(i);
            Feature f = rowFeats.get(i);
            boolean on = f.on();
            int x1 = b.getX(), y1 = b.getY(), x2 = x1 + b.getWidth(), y2 = y1 + b.getHeight();

            box(ctx, x1, y1, x2, y2, hover(b, mouseX, mouseY) ? 0xFF222844 : 0xFF181C30);
            if (on) {
                ctx.fill(x1, y1 + 2, x1 + 2, y2 - 2, Neon.argb(i * 0.07f));
            }
            ctx.drawTextWithShadow(textRenderer, Text.literal(f.title), x1 + 7, y1 + 4, on ? 0xFFFFFFFF : 0xFF8A90A5);

            int sx = x2 - 24, sy = y1 + 3;
            box(ctx, sx, sy, sx + 20, sy + 10, on ? Neon.argb(i * 0.07f) : 0xFF2A2E3D);
            int kx = on ? sx + 11 : sx + 1;
            box(ctx, kx, sy + 1, kx + 8, sy + 9, 0xFFFFFFFF);
        }

        // motywy
        for (int t = 0; t < themeBtns.size(); t++) {
            ButtonWidget b = themeBtns.get(t);
            boolean sel = NeonConfig.theme == t;
            int x1 = b.getX(), y1 = b.getY(), x2 = x1 + b.getWidth(), y2 = y1 + b.getHeight();

            box(ctx, x1, y1, x2, y2, hover(b, mouseX, mouseY) ? 0xFF222844 : 0xFF181C30);
            if (sel) {
                frame(ctx, x1, y1, x2, y2, Neon.themeArgb(t, 0f));
            }
            ctx.drawTextWithShadow(textRenderer, Text.literal(NeonConfig.THEME_NAMES[t]), x1 + 7, y1 + 4,
                    sel ? 0xFFFFFFFF : 0xFFB0B8C8);
            for (int s = 0; s < 4; s++) {
                ctx.fill(x2 - 8 - (4 - s) * 8, y1 + 4, x2 - 8 - (4 - s) * 8 + 6, y1 + 12, Neon.themeArgb(t, s * 0.12f));
            }
        }

        Text hint = Text.literal("ESC zamyka menu  |  /visuals też je otwiera");
        ctx.drawTextWithShadow(textRenderer, hint, (width - textRenderer.getWidth(hint)) / 2, height - 14, 0xFF8A90A5);
    }

    @Override
    public void close() {
        NeonConfig.save();
        super.close();
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
