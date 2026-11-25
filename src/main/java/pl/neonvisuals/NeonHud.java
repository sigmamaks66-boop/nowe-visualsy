package pl.neonvisuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.Text;

/** Celownik, hit marker, Target HUD i znak wodny. */
public final class NeonHud {
    private NeonHud() {}

    public static void render(DrawContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.options.hudHidden) return;

        int cx = ctx.getScaledWindowWidth() / 2;
        int cy = ctx.getScaledWindowHeight() / 2;
        boolean firstPerson = mc.options.getPerspective().isFirstPerson();

        if (Feature.CROSSHAIR.on() && mc.currentScreen == null && firstPerson) {
            arm(ctx, cx - 8, cy, cx - 3, cy + 1, Neon.argb(0.00f));
            arm(ctx, cx + 4, cy, cx + 9, cy + 1, Neon.argb(0.25f));
            arm(ctx, cx, cy - 8, cx + 1, cy - 3, Neon.argb(0.50f));
            arm(ctx, cx, cy + 4, cx + 1, cy + 9, Neon.argb(0.75f));
            ctx.fill(cx, cy, cx + 1, cy + 1, 0xFFFFFFFF);
        }

        int age = Neon.tick - Neon.lastHitTick;
        if (Feature.HIT_MARKER.on() && mc.currentScreen == null && age >= 0 && age < 10) {
            int a = (int) (255 * (1 - age / 10f));
            int col = (a << 24) | 0xFFFFFF;
            for (int k = 4; k <= 8; k++) {
                ctx.fill(cx + k, cy + k, cx + k + 2, cy + k + 2, col);
                ctx.fill(cx - k - 1, cy + k, cx - k + 1, cy + k + 2, col);
                ctx.fill(cx + k, cy - k - 1, cx + k + 2, cy - k + 1, col);
                ctx.fill(cx - k - 1, cy - k - 1, cx - k + 1, cy - k + 1, col);
            }
        }

        LivingEntity t = Neon.hudTarget;
        if (Feature.TARGET_HUD.on() && t != null && mc.currentScreen == null) {
            int x = cx + 26, y = cy + 14, w = 104, h = 28;
            ctx.fill(x + 1, y, x + w - 1, y + h, 0xC0121420);
            ctx.fill(x, y + 1, x + w, y + h - 1, 0xC0121420);
            ctx.fill(x, y + 2, x + 2, y + h - 2, Neon.argb(0f));
            ctx.drawTextWithShadow(mc.textRenderer, t.getName(), x + 7, y + 5, 0xFFFFFFFF);
            float frac = Math.max(0f, Math.min(1f, t.getHealth() / Math.max(1f, t.getMaxHealth())));
            ctx.fill(x + 7, y + 18, x + w - 7, y + 23, 0xFF2A2E3D);
            ctx.fill(x + 7, y + 18, x + 7 + (int) ((w - 14) * frac), y + 23, Neon.argb(0.1f));
        }

        if (Feature.WATERMARK.on()) {
            ctx.drawTextWithShadow(mc.textRenderer, Neon.rainbowText(Brand.NAME), 6, 6, 0xFFFFFFFF);
            ctx.drawTextWithShadow(mc.textRenderer, Text.literal("by " + Brand.AUTHOR), 6, 17, 0xFF8A90A5);
        }
    }

    private static void arm(DrawContext ctx, int x1, int y1, int x2, int y2, int color) {
        ctx.fill(x1 - 1, y1 - 1, x2 + 1, y2 + 1, 0xB0000000);
        ctx.fill(x1, y1, x2, y2, color);
    }
}
