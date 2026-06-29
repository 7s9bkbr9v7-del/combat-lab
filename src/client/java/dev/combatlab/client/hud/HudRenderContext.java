package dev.combatlab.client.hud;

import dev.combatlab.client.state.ClientGameState;
import net.minecraft.client.gui.Font;

public record HudRenderContext(
    Font font,
    HudRectangle bounds,
    int screenWidth,
    int screenHeight,
    boolean editorPreview,
    float frameDeltaTicks,
    ClientGameState gameState) {
  public HudRenderContext(
      Font font,
      HudRectangle bounds,
      int screenWidth,
      int screenHeight,
      ClientGameState gameState) {
    this(font, bounds, screenWidth, screenHeight, false, 1.0F, gameState);
  }

  public HudRenderContext(
      Font font,
      HudRectangle bounds,
      int screenWidth,
      int screenHeight,
      float frameDeltaTicks,
      ClientGameState gameState) {
    this(font, bounds, screenWidth, screenHeight, false, frameDeltaTicks, gameState);
  }

  public HudGameState hud() {
    return gameState.hud();
  }

  public HudOrientation orientation() {
    return HudOrientationResolver.resolve(bounds, screenWidth, screenHeight);
  }
}
