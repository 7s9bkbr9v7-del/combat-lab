package dev.combatlab.client.screen;

record HsvColor(double hue, double saturation, double value) {
  static HsvColor fromRgb(int color) {
    double red = ((color >> 16) & 0xFF) / 255.0D;
    double green = ((color >> 8) & 0xFF) / 255.0D;
    double blue = (color & 0xFF) / 255.0D;
    double max = Math.max(red, Math.max(green, blue));
    double min = Math.min(red, Math.min(green, blue));
    double delta = max - min;
    double hue;
    if (delta == 0.0D) {
      hue = 0.0D;
    } else if (max == red) {
      hue = ((green - blue) / delta) % 6.0D;
    } else if (max == green) {
      hue = (blue - red) / delta + 2.0D;
    } else {
      hue = (red - green) / delta + 4.0D;
    }
    hue /= 6.0D;
    if (hue < 0.0D) {
      hue += 1.0D;
    }
    double saturation = max == 0.0D ? 0.0D : delta / max;
    return new HsvColor(hue, saturation, max);
  }

  static int toRgb(double hue, double saturation, double value) {
    saturation = Math.clamp(saturation, 0.0D, 1.0D);
    value = Math.clamp(value, 0.0D, 1.0D);
    double scaledHue = Math.clamp(hue, 0.0D, 1.0D) * 6.0D;
    double chroma = value * saturation;
    double x = chroma * (1.0D - Math.abs(scaledHue % 2.0D - 1.0D));
    double match = value - chroma;
    double red;
    double green;
    double blue;
    if (scaledHue < 1.0D) {
      red = chroma;
      green = x;
      blue = 0.0D;
    } else if (scaledHue < 2.0D) {
      red = x;
      green = chroma;
      blue = 0.0D;
    } else if (scaledHue < 3.0D) {
      red = 0.0D;
      green = chroma;
      blue = x;
    } else if (scaledHue < 4.0D) {
      red = 0.0D;
      green = x;
      blue = chroma;
    } else if (scaledHue < 5.0D) {
      red = x;
      green = 0.0D;
      blue = chroma;
    } else {
      red = chroma;
      green = 0.0D;
      blue = x;
    }
    return channel(red + match) << 16 | channel(green + match) << 8 | channel(blue + match);
  }

  private static int channel(double value) {
    return Math.clamp((int) Math.round(value * 255.0D), 0, 255);
  }
}
