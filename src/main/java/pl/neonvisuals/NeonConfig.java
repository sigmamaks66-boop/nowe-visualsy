package pl.neonvisuals;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public final class NeonConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("neonvisuals.json");

    public static final String[] THEME_NAMES = {"Rainbow", "Pink", "Blue", "Green", "Purple", "Fire"};

    public static Map<String, Boolean> modules = new HashMap<>();
    public static int theme = 0;

    private static final class Data {
        Map<String, Boolean> modules = new HashMap<>();
        int theme = 0;
    }

    private NeonConfig() {}

    public static void load() {
        try {
            if (Files.exists(FILE)) {
                Data d = GSON.fromJson(Files.readString(FILE), Data.class);
                if (d != null) {
                    if (d.modules != null) modules = d.modules;
                    theme = Math.floorMod(d.theme, THEME_NAMES.length);
                }
            }
        } catch (Exception e) {
            System.err.println("[Neon] Nie udało się wczytać configu: " + e);
        }
    }

    public static void save() {
        try {
            Data d = new Data();
            d.modules = modules;
            d.theme = theme;
            Files.writeString(FILE, GSON.toJson(d));
        } catch (IOException e) {
            System.err.println("[Neon] Nie udało się zapisać configu: " + e);
        }
    }
}
