package dev.combatlab.client.mixin;

import dev.combatlab.client.feature.DynamicFovController;
import net.minecraft.client.Camera;
import net.minecraft.client.OptionInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Camera.class)
abstract class CameraMixin {
	@Redirect(
			method = {"tickFov", "modifyFovBasedOnDeathOrFluid"},
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/OptionInstance;get()Ljava/lang/Object;")
	)
	private Object combatlab$dynamicFovScale(OptionInstance<?> option) {
		Object configuredScale = option.get();
		return DynamicFovController.enabled() ? configuredScale : 0.0D;
	}
}
