package dev.combatlab.client.hud;

import net.minecraft.client.gui.Font;

public final class HudRenderContext {
  private Font font;
  private HudRectangle bounds;
  private int screenWidth;
  private int screenHeight;
  private boolean editorPreview;
  private float frameDeltaTicks;
  private HudGameState hud;

  public HudRenderContext(
      Font font, HudRectangle bounds, int screenWidth, int screenHeight, HudGameState gameState) {
    this(font, bounds, screenWidth, screenHeight, false, 1.0F, gameState);
  }

  public HudRenderContext(
      Font font,
      HudRectangle bounds,
      int screenWidth,
      int screenHeight,
      float frameDeltaTicks,
      HudGameState gameState) {
    this(font, bounds, screenWidth, screenHeight, false, frameDeltaTicks, gameState);
  }

  public HudRenderContext(
      Font font,
      HudRectangle bounds,
      int screenWidth,
      int screenHeight,
      boolean editorPreview,
      float frameDeltaTicks,
      HudGameState hud) {
    update(font, bounds, screenWidth, screenHeight, editorPreview, frameDeltaTicks, hud);
  }

  void update(
      Font font,
      HudRectangle bounds,
      int screenWidth,
      int screenHeight,
      float frameDeltaTicks,
      HudGameState hud) {
    update(font, bounds, screenWidth, screenHeight, false, frameDeltaTicks, hud);
  }

  private void update(
      Font font,
      HudRectangle bounds,
      int screenWidth,
      int screenHeight,
      boolean editorPreview,
      float frameDeltaTicks,
      HudGameState hud) {
    this.font = font;
    this.bounds = bounds;
    this.screenWidth = screenWidth;
    this.screenHeight = screenHeight;
    this.editorPreview = editorPreview;
    this.frameDeltaTicks = frameDeltaTicks;
    this.hud = hud == null ? HudGameState.empty() : hud;
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

  public HudGameState hud() {
    return hud;
  }

  public HudOrientation orientation() {
    return HudOrientationResolver.resolve(bounds, screenWidth, screenHeight);
  }
}
