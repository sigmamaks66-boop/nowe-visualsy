package pl.neonvisuals.mixin;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pl.neonvisuals.Feature;
import pl.neonvisuals.NeonHud;

@Mixin(InGameHud.class)
public class InGameHudMixin {
    @Inject(method = "render", at = @At("TAIL"))
    private void neon$render(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        NeonHud.render(context);
    }

    @Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
    private void neon$hideCrosshair(CallbackInfo ci) {
        if (Feature.CROSSHAIR.on()) {
            ci.cancel();
        }
    }
}
