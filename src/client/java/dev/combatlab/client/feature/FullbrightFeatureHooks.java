package dev.combatlab.client.feature;

import net.minecraft.client.renderer.LightmapRenderStateExtractor;
import net.minecraft.client.renderer.state.LightmapRenderState;

public final class FullbrightFeatureHooks {
  private FullbrightFeatureHooks() {}

  public static Result apply(LightmapRenderState state, boolean wasFullbright) {
    if (FullbrightController.enabled()) {
      if (state.needsUpdate || !wasFullbright) {
        state.needsUpdate = true;
        state.nightVisionEffectIntensity = 1.0F;
        state.nightVisionColor = LightmapRenderStateExtractor.WHITE;
        state.darknessEffectScale = 0.0F;
        state.bossOverlayWorldDarkening = 0.0F;
      }
      return new Result(true, false);
    }

    return wasFullbright ? new Result(false, true) : new Result(false, false);
  }

  public record Result(boolean wasFullbright, boolean extractorNeedsUpdate) {}
}
