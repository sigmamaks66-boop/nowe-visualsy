package pl.neonvisuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

import java.util.Random;

/** Kolory i efekty (wszystko po stronie klienta, to zwykłe cząsteczki i HUD). */
public final class Neon {
    private static final double TAU = Math.PI * 2;
    private static final Random R = new Random();

    public static int tick;
    public static int lastHitTick = -1000;
    public static LivingEntity hudTarget;

    private static int hudTargetAge;
    private static LivingEntity killTarget;
    private static int killTargetAge;
    private static double lastPx, lastPy, lastPz;
    private static boolean wasOnGround = true;

    private Neon() {}

    // ---------- kolory ----------

    public static float time() {
        return (System.currentTimeMillis() % 6000L) / 6000f;
    }

    public static int hsv(float h, float s, float v) {
        h = h - (float) Math.floor(h);
        float hh = h * 6f;
        int i = (int) hh;
        float f = hh - i;
        float p = v * (1 - s);
        float q = v * (1 - f * s);
        float t = v * (1 - (1 - f) * s);
        float r, g, b;
        switch (i % 6) {
            case 0 -> { r = v; g = t; b = p; }
            case 1 -> { r = q; g = v; b = p; }
            case 2 -> { r = p; g = v; b = t; }
            case 3 -> { r = p; g = q; b = v; }
            case 4 -> { r = t; g = p; b = v; }
            default -> { r = v; g = p; b = q; }
        }
        return ((int) (r * 255) << 16) | ((int) (g * 255) << 8) | (int) (b * 255);
    }

    public static int themeRgb(int theme, float offset) {
        float t = time() + offset;
        float wave = (float) Math.sin(t * TAU);
        return switch (theme) {
            case 1 -> hsv(0.86f + 0.07f * wave, 0.75f, 1f);
            case 2 -> hsv(0.56f + 0.05f * wave, 0.8f, 1f);
            case 3 -> hsv(0.36f + 0.05f * wave, 0.8f, 1f);
            case 4 -> hsv(0.76f + 0.05f * wave, 0.75f, 1f);
            case 5 -> hsv(0.04f + 0.04f * wave, 0.85f, 1f);
            default -> hsv(t, 0.8f, 1f);
        };
    }

    public static int rgb(float offset) {
        return themeRgb(NeonConfig.theme, offset);
    }

    public static int argb(float offset) {
        return 0xFF000000 | rgb(offset);
    }

    public static int themeArgb(int theme, float offset) {
        return 0xFF000000 | themeRgb(theme, offset);
    }

    public static MutableText rainbowText(String s) {
        MutableText out = Text.empty();
        for (int i = 0; i < s.length(); i++) {
            out.append(Text.literal(String.valueOf(s.charAt(i)))
                    .setStyle(Style.EMPTY.withColor(rgb(i * 0.06f)).withBold(true)));
        }
        return out;
    }

    // ---------- cząsteczki ----------

    private static void spawn(ClientWorld w, ParticleEffect e, double x, double y, double z,
                              double vx, double vy, double vz, boolean important) {
        if (important) {
            w.addImportantParticleClient(e, x, y, z, vx, vy, vz);
        } else {
            w.addParticleClient(e, x, y, z, vx, vy, vz);
        }
    }

    private static void dust(ClientWorld w, double x, double y, double z,
                             double vx, double vy, double vz, int rgb, float scale, boolean important) {
        spawn(w, new DustParticleEffect(rgb, scale), x, y, z, vx, vy, vz, important);
    }

    private static double rnd() {
        return R.nextDouble() * 2 - 1;
    }

    public static void hitBurst(Entity e) {
        ClientWorld w = MinecraftClient.getInstance().world;
        if (w == null) return;
        double cx = e.getX(), cy = e.getY() + e.getHeight() * 0.6, cz = e.getZ();
        for (int i = 0; i < 18; i++) {
            double a = R.nextDouble() * TAU;
            double sp = 0.06 + R.nextDouble() * 0.1;
            dust(w, cx, cy, cz, Math.cos(a) * sp, (R.nextDouble() - 0.3) * 0.2, Math.sin(a) * sp,
                    rgb(i * 0.05f), 0.9f, false);
        }
        for (int i = 0; i < 5; i++) {
            spawn(w, ParticleTypes.END_ROD, cx, cy, cz, rnd() * 0.1, rnd() * 0.1, rnd() * 0.1, false);
        }
    }

    public static void killBurst(double x, double y, double z) {
        ClientWorld w = MinecraftClient.getInstance().world;
        if (w == null) return;
        for (int ring = 0; ring < 3; ring++) {
            int n = 28;
            for (int i = 0; i < n; i++) {
                double a = i * TAU / n;
                double sp = 0.12 + ring * 0.05;
                dust(w, x, y + 0.2 + ring * 0.55, z, Math.cos(a) * sp, 0.02, Math.sin(a) * sp,
                        rgb(i / (float) n + ring * 0.2f), 1.1f, true);
            }
        }
        for (int i = 0; i < 26; i++) {
            dust(w, x + rnd() * 0.15, y + i * 0.1, z + rnd() * 0.15, 0, 0.1 + R.nextDouble() * 0.1, 0,
                    rgb(i * 0.04f), 1.2f, true);
        }
        for (int i = 0; i < 40; i++) {
            spawn(w, ParticleTypes.TOTEM_OF_UNDYING, x, y + 1.0, z,
                    rnd() * 0.4, 0.1 + R.nextDouble() * 0.5, rnd() * 0.4, true);
        }
    }

    public static void testBurst() {
        ClientPlayerEntity p = MinecraftClient.getInstance().player;
        if (p == null) return;
        Vec3d v = p.getRotationVec(1f);
        killBurst(p.getX() + v.x * 2.5, p.getY(), p.getZ() + v.z * 2.5);
    }

    private static void jumpCircle(ClientWorld w, ClientPlayerEntity p) {
        for (int ring = 0; ring < 2; ring++) {
            int n = 30;
            for (int i = 0; i < n; i++) {
                double a = i * TAU / n;
                double sp = 0.10 + ring * 0.06;
                dust(w, p.getX(), p.getY() + 0.05, p.getZ(), Math.cos(a) * sp, 0.0, Math.sin(a) * sp,
                        rgb(i / (float) n + ring * 0.15f), 0.8f, false);
            }
        }
    }

    // ---------- zdarzenia ----------

    public static void onAttack(Entity e) {
        if (!(e instanceof LivingEntity le)) return;
        lastHitTick = tick;
        hudTarget = le;
        hudTargetAge = 0;
        if (Feature.HIT_PARTICLES.on()) hitBurst(e);
        if (Feature.KILL_EFFECT.on() && e instanceof PlayerEntity) {
            killTarget = le;
            killTargetAge = 0;
        }
    }

    public static void clientTick(MinecraftClient mc) {
        ClientPlayerEntity p = mc.player;
        ClientWorld w = mc.world;
        if (p == null || w == null) {
            killTarget = null;
            hudTarget = null;
            return;
        }
        tick++;

        if (hudTarget != null && (hudTarget.isRemoved() || ++hudTargetAge > 100)) {
            hudTarget = null;
        }

        if (killTarget != null) {
            if (killTarget.getHealth() <= 0.0f) {
                killBurst(killTarget.getX(), killTarget.getY(), killTarget.getZ());
                killTarget = null;
            } else if (killTarget.isRemoved() || ++killTargetAge > 120) {
                killTarget = null;
            }
        }

        boolean hidden = p.isSpectator();

        // chiński kapelusz: płaski dysk z dwóch pierścieni
        if (Feature.CHINA_HAT.on() && !hidden) {
            double y = p.getY() + p.getHeight() + 0.03;
            double spin = tick * 0.05;
            for (int i = 0; i < 22; i++) {
                double a = spin + i * TAU / 22;
                dust(w, p.getX() + Math.cos(a) * 0.72, y, p.getZ() + Math.sin(a) * 0.72, 0, 0, 0,
                        rgb(i / 22f), 0.6f, false);
            }
            for (int i = 0; i < 14; i++) {
                double a = -spin + i * TAU / 14;
                dust(w, p.getX() + Math.cos(a) * 0.45, y + 0.01, p.getZ() + Math.sin(a) * 0.45, 0, 0, 0,
                        rgb(i / 14f + 0.3f), 0.5f, false);
            }
        }

        // halo nad głową
        if (Feature.HALO.on() && !hidden) {
            double y = p.getY() + p.getHeight() + 0.4;
            double spin = tick * 0.12;
            for (int i = 0; i < 14; i++) {
                double a = spin + i * TAU / 14;
                dust(w, p.getX() + Math.cos(a) * 0.32, y, p.getZ() + Math.sin(a) * 0.32, 0, 0, 0,
                        rgb(i / 14f), 0.5f, false);
            }
        }

        // koła przy skoku
        boolean onGround = p.isOnGround();
        if (Feature.JUMP_CIRCLES.on() && wasOnGround && !onGround && p.getVelocity().y > 0.1) {
            jumpCircle(w, p);
        }
        wasOnGround = onGround;

        // ślad
        if (Feature.TRAILS.on()) {
            double dx = p.getX() - lastPx, dy = p.getY() - lastPy, dz = p.getZ() - lastPz;
            if (dx * dx + dy * dy + dz * dz > 0.0016) {
                for (int k = 0; k < 3; k++) {
                    dust(w, p.getX() + rnd() * 0.2, p.getY() + 0.1 + R.nextDouble() * 0.2, p.getZ() + rnd() * 0.2,
                            0, 0.02, 0, rgb(k * 0.1f), 0.9f, false);
                }
            }
        }
        lastPx = p.getX();
        lastPy = p.getY();
        lastPz = p.getZ();

        // cząsteczki w powietrzu wokół Ciebie
        if (Feature.AMBIENT.on()) {
            for (int k = 0; k < 2; k++) {
                dust(w, p.getX() + rnd() * 4, p.getY() + R.nextDouble() * 2.5, p.getZ() + rnd() * 4,
                        0, 0.02, 0, rgb(R.nextFloat()), 0.6f, false);
            }
        }
    }
}
