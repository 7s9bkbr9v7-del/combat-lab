package dev.combatlab.client.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class HsvColorTest {
  @Test
  void convertsPrimaryColorsBetweenRgbAndHsv() {
    assertEquals(0xFF0000, HsvColor.toRgb(0.0D, 1.0D, 1.0D));
    assertEquals(0x00FF00, HsvColor.toRgb(1.0D / 3.0D, 1.0D, 1.0D));
    assertEquals(0x0000FF, HsvColor.toRgb(2.0D / 3.0D, 1.0D, 1.0D));

    assertEquals(0.0D, HsvColor.fromRgb(0xFF0000).hue(), 0.0001D);
    assertEquals(1.0D / 3.0D, HsvColor.fromRgb(0x00FF00).hue(), 0.0001D);
    assertEquals(2.0D / 3.0D, HsvColor.fromRgb(0x0000FF).hue(), 0.0001D);
  }

  @Test
  void preservesNeutralValueWithoutSaturation() {
    HsvColor gray = HsvColor.fromRgb(0x808080);

    assertEquals(0.0D, gray.saturation(), 0.0001D);
    assertEquals(0x808080, HsvColor.toRgb(gray.hue(), gray.saturation(), gray.value()));
  }
}
