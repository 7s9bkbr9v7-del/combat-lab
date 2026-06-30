package dev.combatlab.client.hud;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class HudTextScaleTest {
  @Test
  void choosesNearestFriendlyScale() {
    assertEquals(0.9D, HudTextScale.nearest(0.92D), 0.0001D);
    assertEquals(1.25D, HudTextScale.nearest(1.31D), 0.0001D);
    assertEquals(2.5D, HudTextScale.nearest(2.4D), 0.0001D);
  }
}
