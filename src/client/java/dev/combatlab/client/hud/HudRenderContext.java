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
  private HudGameState hud;

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
    this(
        font,
        bounds,
        screenWidth,
        screenHeight,
        editorPreview,
        frameDeltaTicks,
        gameState,
        HudGameState.from(gameState));
  }

  HudRenderContext(
      Font font,
      HudRectangle bounds,
      int screenWidth,
      int screenHeight,
      boolean editorPreview,
      float frameDeltaTicks,
      ClientGameState gameState,
      HudGameState hud) {
    update(font, bounds, screenWidth, screenHeight, editorPreview, frameDeltaTicks, gameState, hud);
  }

  void update(
      Font font,
      HudRectangle bounds,
      int screenWidth,
      int screenHeight,
      float frameDeltaTicks,
      ClientGameState gameState) {
    update(
        font,
        bounds,
        screenWidth,
        screenHeight,
        false,
        frameDeltaTicks,
        gameState,
        HudGameState.from(gameState));
  }

  private void update(
      Font font,
      HudRectangle bounds,
      int screenWidth,
      int screenHeight,
      boolean editorPreview,
      float frameDeltaTicks,
      ClientGameState gameState,
      HudGameState hud) {
    this.font = font;
    this.bounds = bounds;
    this.screenWidth = screenWidth;
    this.screenHeight = screenHeight;
    this.editorPreview = editorPreview;
    this.frameDeltaTicks = frameDeltaTicks;
    this.gameState = gameState;
    this.hud = hud;
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
    return hud;
  }

  public HudOrientation orientation() {
    return HudOrientationResolver.resolve(bounds, screenWidth, screenHeight);
  }
}
