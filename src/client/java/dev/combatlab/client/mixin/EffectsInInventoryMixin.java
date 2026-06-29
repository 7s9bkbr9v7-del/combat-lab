package dev.combatlab.client.mixin;

import dev.combatlab.client.feature.FullbrightController;
import java.util.Collection;
import net.minecraft.client.gui.screens.inventory.EffectsInInventory;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EffectsInInventory.class)
abstract class EffectsInInventoryMixin {
  @Redirect(
      method = "extractRenderState",
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/client/player/LocalPlayer;getActiveEffects()Ljava/util/Collection;"))
  private Collection<MobEffectInstance> combatlab$hideFullbrightNightVision(LocalPlayer player) {
    Collection<MobEffectInstance> effects = player.getActiveEffects();
    if (!FullbrightController.enabled()) {
      return effects;
    }
    return effects.stream()
        .filter(effect -> !FullbrightController.shouldHideEffectStatus(effect))
        .toList();
  }
}
