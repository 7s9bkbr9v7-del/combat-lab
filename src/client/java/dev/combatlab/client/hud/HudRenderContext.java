package dev.combatlab.client.hud;

import dev.combatlab.client.state.ClientGameState;
import net.minecraft.client.gui.Font;

public record HudRenderContext(
    Font font,
    HudRectangle bounds,
    int screenWidth,
    int screenHeight,
    boolean editorPreview,
    ClientGameState gameState) {
  public HudRenderContext(
      Font font,
      HudRectangle bounds,
      int screenWidth,
      int screenHeight,
      ClientGameState gameState) {
    this(font, bounds, screenWidth, screenHeight, false, gameState);
  }

  public HudGameState hud() {
    return gameState.hud();
  }

  public HudOrientation orientation() {
    return HudOrientationResolver.resolve(bounds, screenWidth, screenHeight);
  }
}
