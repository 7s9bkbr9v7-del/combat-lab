package dev.combatlab.client.mixin;

import dev.combatlab.client.feature.CameraFeatureHooks;
import net.minecraft.client.Camera;
import net.minecraft.client.CameraType;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Camera.class)
abstract class CameraMixin {
  @Redirect(
      method = {"tickFov", "modifyFovBasedOnDeathOrFluid"},
      at =
          @At(
              value = "INVOKE",
              target = "Lnet/minecraft/client/OptionInstance;get()Ljava/lang/Object;"))
  private Object combatlab$dynamicFovScale(OptionInstance<?> option) {
    return CameraFeatureHooks.dynamicFovScale(option);
  }

  @Redirect(
      method = "alignWithEntity",
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/client/Options;getCameraType()Lnet/minecraft/client/CameraType;"))
  private CameraType combatlab$freelookCameraType(Options options) {
    return CameraFeatureHooks.cameraType(options);
  }

  @ModifyArg(
      method = "alignWithEntity",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setRotation(FF)V"),
      index = 0)
  private float combatlab$freelookYaw(float yaw) {
    return CameraFeatureHooks.yaw(yaw);
  }

  @ModifyArg(
      method = "alignWithEntity",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setRotation(FF)V"),
      index = 1)
  private float combatlab$freelookPitch(float pitch) {
    return CameraFeatureHooks.pitch(pitch);
  }

  @Inject(method = "calculateFov", at = @At("RETURN"), cancellable = true)
  private void combatlab$applyZoom(float partialTick, CallbackInfoReturnable<Float> callbackInfo) {
    callbackInfo.setReturnValue(CameraFeatureHooks.fov(callbackInfo.getReturnValueF()));
  }
}
