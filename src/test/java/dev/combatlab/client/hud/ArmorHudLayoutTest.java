package dev.combatlab.client.hud;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ArmorHudLayoutTest {
  @Test
  void usesVerticalLayoutOnlyOnLeftOrRightEdgesAwayFromCorners() {
    assertEquals(ArmorHudLayout.VERTICAL, ArmorHudLayout.resolve(0.0, 0.5));
    assertEquals(ArmorHudLayout.VERTICAL, ArmorHudLayout.resolve(1.0, 0.5));
  }

  @Test
  void usesHorizontalLayoutOnlyOnTopOrBottomEdgesAwayFromCorners() {
    assertEquals(ArmorHudLayout.HORIZONTAL, ArmorHudLayout.resolve(0.5, 0.0));
    assertEquals(ArmorHudLayout.HORIZONTAL, ArmorHudLayout.resolve(0.5, 1.0));
  }

  @Test
  void usesGridLayoutInCornersAndAwayFromScreenEdges() {
    assertEquals(ArmorHudLayout.GRID, ArmorHudLayout.resolve(0.0, 0.0));
    assertEquals(ArmorHudLayout.GRID, ArmorHudLayout.resolve(1.0, 1.0));
    assertEquals(ArmorHudLayout.GRID, ArmorHudLayout.resolve(0.5, 0.5));
  }

  @Test
  void keepsThePreviousLayoutAwayFromScreenEdges() {
    assertEquals(
        ArmorHudLayout.VERTICAL, ArmorHudLayout.resolve(0.5, 0.5, ArmorHudLayout.VERTICAL));
    assertEquals(
        ArmorHudLayout.HORIZONTAL, ArmorHudLayout.resolve(0.5, 0.5, ArmorHudLayout.HORIZONTAL));
  }

  @Test
  void safelyDefaultsUnknownStoredLayoutsToGrid() {
    assertEquals(ArmorHudLayout.GRID, ArmorHudLayout.fromStored(null));
    assertEquals(ArmorHudLayout.GRID, ArmorHudLayout.fromStored("unknown"));
    assertEquals(ArmorHudLayout.VERTICAL, ArmorHudLayout.fromStored("VERTICAL"));
  }
}
