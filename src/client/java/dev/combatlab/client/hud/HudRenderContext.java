package dev.combatlab.client.hud;

import dev.combatlab.client.state.ClientGameState;
import net.minecraft.client.gui.Font;

public final class HudRenderContext {
  private Font font;
  private HudRectangle bounds;
  private int screenWidth;
  private int screenHeight;
  private boolean editorPreview;
  private float frameDeltaTicks;
  private ClientGameState gameState;

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

  public HudRenderContext(
      Font font,
      HudRectangle bounds,
      int screenWidth,
      int screenHeight,
      boolean editorPreview,
      float frameDeltaTicks,
      ClientGameState gameState) {
    update(font, bounds, screenWidth, screenHeight, editorPreview, frameDeltaTicks, gameState);
  }

  void update(
      Font font,
      HudRectangle bounds,
      int screenWidth,
      int screenHeight,
      float frameDeltaTicks,
      ClientGameState gameState) {
    update(font, bounds, screenWidth, screenHeight, false, frameDeltaTicks, gameState);
  }

  private void update(
      Font font,
      HudRectangle bounds,
      int screenWidth,
      int screenHeight,
      boolean editorPreview,
      float frameDeltaTicks,
      ClientGameState gameState) {
    this.font = font;
    this.bounds = bounds;
    this.screenWidth = screenWidth;
    this.screenHeight = screenHeight;
    this.editorPreview = editorPreview;
    this.frameDeltaTicks = frameDeltaTicks;
    this.gameState = gameState;
  }

  public Font font() {
    return font;
  }

  public HudRectangle bounds() {
    return bounds;
  }

  public int screenWidth() {
    return screenWidth;
  }

  public int screenHeight() {
    return screenHeight;
  }

  public boolean editorPreview() {
    return editorPreview;
  }

  public float frameDeltaTicks() {
    return frameDeltaTicks;
  }

  public ClientGameState gameState() {
    return gameState;
  }

  public HudGameState hud() {
    return gameState.hud();
  }

  public HudOrientation orientation() {
    return HudOrientationResolver.resolve(bounds, screenWidth, screenHeight);
  }
}
