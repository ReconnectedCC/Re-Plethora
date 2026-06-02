package io.sc3.plethora.mixin.client;

import io.sc3.plethora.gameplay.modules.glasses.canvas.CanvasHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {
    @Shadow
    @Final
    private MinecraftClient client;

    @Inject(method = "render", at = @At("TAIL"))
    private void render(DrawContext ctx, RenderTickCounter tickCounter, CallbackInfo ci) {
        this.client.getProfiler().push("plethora:renderCanvas2DOverlay");
        CanvasHandler.render2DOverlay(client, ctx);
        this.client.getProfiler().pop();
    }
}
