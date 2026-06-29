package dev.combatlab.client.hud;

public final class HudLayout {
  private HudLayout() {}

  public static HudPosition resolve(
      double normalizedX, double normalizedY, int screenWidth, int screenHeight, HudSize size) {
    int travelX = Math.max(0, screenWidth - size.width());
    int travelY = Math.max(0, screenHeight - size.height());
    return new HudPosition(
        (int) Math.round(clamp(normalizedX) * travelX),
        (int) Math.round(clamp(normalizedY) * travelY));
  }

  public static double normalizeX(int x, int screenWidth, HudSize size) {
    return normalize(x, Math.max(0, screenWidth - size.width()));
  }

  public static double normalizeY(int y, int screenHeight, HudSize size) {
    return normalize(y, Math.max(0, screenHeight - size.height()));
  }

  private static double normalize(int value, int travel) {
    return travel == 0 ? 0.0 : clamp((double) value / travel);
  }

  private static double clamp(double value) {
    return Math.clamp(value, 0.0, 1.0);
  }
}
