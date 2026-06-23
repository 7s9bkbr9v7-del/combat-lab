package dev.combatlab.client.mixin;

import dev.combatlab.client.feature.ToastFeatureHooks;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ToastManager.class)
abstract class ToastManagerMixin {
	@Inject(method = "addToast", at = @At("HEAD"), cancellable = true)
	private void combatlab$suppressAdvancementToasts(Toast toast, CallbackInfo callbackInfo) {
		if (ToastFeatureHooks.shouldSuppress(toast)) {
			callbackInfo.cancel();
		}
	}
}
