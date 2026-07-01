package dev.combatlab.client.feature;

import net.minecraft.client.renderer.LightmapRenderStateExtractor;
import net.minecraft.client.renderer.state.LightmapRenderState;

public final class FullbrightFeatureHooks {
  private static final Result ACTIVE = new Result(true, false);
  private static final Result DISABLED_AFTER_ACTIVE = new Result(false, true);
  private static final Result DISABLED = new Result(false, false);

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
      return ACTIVE;
    }

    return wasFullbright ? DISABLED_AFTER_ACTIVE : DISABLED;
  }

  public record Result(boolean wasFullbright, boolean extractorNeedsUpdate) {}
}
