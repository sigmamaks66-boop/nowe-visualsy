package pl.neonvisuals;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class NeonClient implements ClientModInitializer {
    private static KeyBinding openKey;

    @Override
    public void onInitializeClient() {
        NeonConfig.load();

        // klawisz otwierający menu: domyślnie "=" (zmienisz w Opcje > Sterowanie > Neon)
        KeyBinding.Category category = KeyBinding.Category.create(Identifier.of("neonvisuals", "main"));
        openKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.neonvisuals.open_gui", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_EQUAL, category));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            Neon.clientTick(client);
            while (openKey.wasPressed()) {
                if (client.currentScreen == null) {
                    client.setScreen(new NeonScreen());
                }
            }
        });

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (world.isClient() && player == MinecraftClient.getInstance().player) {
                Neon.onAttack(entity);
            }
            return ActionResult.PASS;
        });

        // zapasowo: /visuals też otwiera menu
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                dispatcher.register(ClientCommandManager.literal("visuals").executes(ctx -> {
                    MinecraftClient mc = ctx.getSource().getClient();
                    mc.send(() -> mc.setScreen(new NeonScreen()));
                    return 1;
                })));
    }
}
