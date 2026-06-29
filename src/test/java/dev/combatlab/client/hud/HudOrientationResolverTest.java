package dev.combatlab.client.hud;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class HudOrientationResolverTest {
  @Test
  void picksTheSidesFacingTheScreenCenter() {
    HudOrientation topLeft =
        HudOrientationResolver.resolve(new HudRectangle(0, 0, 80, 20), 1000, 500);
    assertEquals(HudHorizontalSide.RIGHT, topLeft.horizontalSideFacingCenter());
    assertEquals(HudVerticalSide.BOTTOM, topLeft.verticalSideFacingCenter());

    HudOrientation bottomRight =
        HudOrientationResolver.resolve(new HudRectangle(920, 480, 80, 20), 1000, 500);
    assertEquals(HudHorizontalSide.LEFT, bottomRight.horizontalSideFacingCenter());
    assertEquals(HudVerticalSide.TOP, bottomRight.verticalSideFacingCenter());
  }

  @Test
  void picksTheCornerClosestToTheScreenCenter() {
    assertEquals(
        HudCorner.BOTTOM_RIGHT,
        HudOrientationResolver.resolve(new HudRectangle(0, 0, 80, 20), 1000, 500)
            .cornerFacingCenter());
    assertEquals(
        HudCorner.TOP_RIGHT,
        HudOrientationResolver.resolve(new HudRectangle(0, 480, 80, 20), 1000, 500)
            .cornerFacingCenter());
    assertEquals(
        HudCorner.TOP_LEFT,
        HudOrientationResolver.resolve(new HudRectangle(920, 480, 80, 20), 1000, 500)
            .cornerFacingCenter());
  }
}
