package dev.combatlab.client.mixin;

import dev.combatlab.client.feature.FullbrightController;
import net.minecraft.client.renderer.LightmapRenderStateExtractor;
import net.minecraft.client.renderer.state.LightmapRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LightmapRenderStateExtractor.class)
abstract class LightmapRenderStateExtractorMixin {
	@Shadow
	private boolean needsUpdate;

	@Unique
	private boolean combatlab$wasFullbright;

	@Inject(method = "extract", at = @At("RETURN"))
	private void combatlab$applyFullbright(LightmapRenderState state, float partialTick, CallbackInfo callbackInfo) {
		if (FullbrightController.enabled()) {
			state.needsUpdate = true;
			state.nightVisionEffectIntensity = 1.0F;
			state.nightVisionColor = LightmapRenderStateExtractor.WHITE;
			state.darknessEffectScale = 0.0F;
			state.bossOverlayWorldDarkening = 0.0F;
			combatlab$wasFullbright = true;
		} else if (combatlab$wasFullbright) {
			needsUpdate = true;
			combatlab$wasFullbright = false;
		}
	}
}
