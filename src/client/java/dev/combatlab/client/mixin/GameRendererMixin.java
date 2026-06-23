package dev.combatlab.client.mixin;

import dev.combatlab.client.feature.FreelookController;
import net.minecraft.client.CameraType;
import net.minecraft.client.Options;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GameRenderer.class)
abstract class GameRendererMixin {
	@Redirect(
			method = "extractOptions",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Options;getCameraType()Lnet/minecraft/client/CameraType;")
	)
	private CameraType combatlab$freelookRenderCameraType(Options options) {
		return FreelookController.active() ? CameraType.THIRD_PERSON_BACK : options.getCameraType();
	}
}
