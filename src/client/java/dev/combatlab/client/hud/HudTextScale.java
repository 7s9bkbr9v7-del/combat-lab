package dev.combatlab.client.hud;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class HudTextScale {
  private static final double[] FRIENDLY_SCALES = {
    0.5D, 0.625D, 0.75D, 0.8D, 0.9D, 1.0D, 1.125D, 1.25D, 1.5D, 1.75D, 2.0D, 2.5D, 3.0D, 4.0D
  };

  private HudTextScale() {}

  public static double nearest(double scale) {
    double nearest = FRIENDLY_SCALES[0];
    double nearestDistance = Math.abs(scale - nearest);
    for (int index = 1; index < FRIENDLY_SCALES.length; index++) {
      double candidate = FRIENDLY_SCALES[index];
      double distance = Math.abs(scale - candidate);
      if (distance < nearestDistance) {
        nearest = candidate;
        nearestDistance = distance;
      }
    }
    return nearest;
  }

  public static double centeredX(Font font, String text, double centerX, double scale) {
    return centerX - font.width(text) * scale / 2.0D;
  }

  public static double centeredY(Font font, double centerY, double scale) {
    return centerY - font.lineHeight * scale / 2.0D;
  }

  public static void draw(
      GuiGraphicsExtractor graphics,
      Font font,
      String text,
      double x,
      double y,
      double scale,
      int color,
      boolean shadow) {
    graphics.pose().pushMatrix();
    graphics.pose().translate((float) x, (float) y);
    graphics.pose().scale((float) scale, (float) scale);
    graphics.text(font, text, 0, 0, color, shadow);
    graphics.pose().popMatrix();
  }
}
