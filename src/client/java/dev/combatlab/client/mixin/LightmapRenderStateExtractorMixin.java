package dev.combatlab.client.mixin;

import dev.combatlab.client.feature.FullbrightFeatureHooks;
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
		FullbrightFeatureHooks.Result result = FullbrightFeatureHooks.apply(state, combatlab$wasFullbright);
		combatlab$wasFullbright = result.wasFullbright();
		if (result.extractorNeedsUpdate()) {
			needsUpdate = true;
		}
	}
}
