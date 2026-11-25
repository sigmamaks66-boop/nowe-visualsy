package pl.neonvisuals;

public enum Feature {
    HIT_PARTICLES(Category.COMBAT, "Hit Particles", true),
    HIT_MARKER(Category.COMBAT, "Hit Marker", true),
    KILL_EFFECT(Category.COMBAT, "Kill Effect", true),
    TARGET_HUD(Category.COMBAT, "Target HUD", true),

    CHINA_HAT(Category.WORLD, "China Hat", true),
    HALO(Category.WORLD, "Halo", false),
    JUMP_CIRCLES(Category.WORLD, "Jump Circles", true),
    TRAILS(Category.WORLD, "Trails", false),
    AMBIENT(Category.WORLD, "Ambient FX", false),

    WATERMARK(Category.HUD, "Watermark", true),
    CROSSHAIR(Category.HUD, "Crosshair", true);

    public enum Category {
        COMBAT("Combat"), WORLD("World"), HUD("HUD");

        public final String title;

        Category(String title) {
            this.title = title;
        }
    }

    public final Category category;
    public final String title;
    private final boolean def;

    Feature(Category category, String title, boolean def) {
        this.category = category;
        this.title = title;
        this.def = def;
    }

    public boolean on() {
        return NeonConfig.modules.getOrDefault(name(), def);
    }

    public void toggle() {
        NeonConfig.modules.put(name(), !on());
    }
}
