package dev.combatlab.client.mixin;

import dev.combatlab.client.feature.MouseFeatureHooks;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MouseHandler.class)
abstract class MouseHandlerMixin {
  @Redirect(
      method = "turnPlayer",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;turn(DD)V"))
  private void combatlab$turnPlayerOrFreelook(LocalPlayer player, double deltaX, double deltaY) {
    MouseFeatureHooks.turnPlayerOrFreelook(player, deltaX, deltaY);
  }
}
