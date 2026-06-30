package dev.combatlab.client.hud;

public final class HudLayout {
  private HudLayout() {}

  public static HudPosition resolve(
      double normalizedX, double normalizedY, int screenWidth, int screenHeight, HudSize size) {
    return new HudPosition(
        resolveX(normalizedX, screenWidth, size), resolveY(normalizedY, screenHeight, size));
  }

  public static int resolveX(double normalizedX, int screenWidth, HudSize size) {
    return resolveAxis(normalizedX, Math.max(0, screenWidth - size.width()));
  }

  public static int resolveY(double normalizedY, int screenHeight, HudSize size) {
    return resolveAxis(normalizedY, Math.max(0, screenHeight - size.height()));
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

  private static int resolveAxis(double normalized, int travel) {
    return (int) Math.round(clamp(normalized) * travel);
  }

  private static double clamp(double value) {
    return Math.clamp(value, 0.0, 1.0);
  }
}
