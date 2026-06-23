package dev.combatlab.client.mixin;

import dev.combatlab.client.compat.MinecraftCapabilities;
import dev.combatlab.client.feature.VanillaHudFeatureHooks;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = {
		MinecraftCapabilities.VANILLA_HUD_STATUS_EFFECT_TARGET_LEGACY_GUI,
		MinecraftCapabilities.VANILLA_HUD_STATUS_EFFECT_TARGET_HUD
})
abstract class VanillaStatusEffectsHudMixin {
	@Inject(method = "extractEffects", at = @At("HEAD"), cancellable = true, require = 0)
	private void combatlab$suppressVanillaStatusEffects(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo callbackInfo) {
		if (VanillaHudFeatureHooks.shouldSuppressStatusEffectsHud()) {
			callbackInfo.cancel();
		}
	}
}
