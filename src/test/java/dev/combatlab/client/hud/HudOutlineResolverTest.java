package dev.combatlab.client.hud;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

class HudOutlineResolverTest {
  @Test
  void removesOnlyTheTouchingPortionOfASide() {
    HudOutlineSegments segments =
        HudOutlineResolver.visibleSegments(
            new HudRectangle(100, 100, 80, 20), List.of(new HudRectangle(150, 120, 60, 20)));

    assertEquals(List.of(new HudOutlineSegment(99, 150)), segments.bottom());
    assertEquals(List.of(new HudOutlineSegment(99, 181)), segments.top());
  }

  @Test
  void subtractsSeveralTouchingIntervalsFromOneSide() {
    HudOutlineSegments segments =
        HudOutlineResolver.visibleSegments(
            new HudRectangle(100, 100, 100, 20),
            List.of(new HudRectangle(110, 120, 20, 20), new HudRectangle(150, 120, 30, 20)));

    assertEquals(
        List.of(
            new HudOutlineSegment(99, 110),
            new HudOutlineSegment(130, 150),
            new HudOutlineSegment(180, 201)),
        segments.bottom());
  }

  @Test
  void hidesTheExactCornerPixelsWhenCornersTouch() {
    HudOutlineSegments segments =
        HudOutlineResolver.visibleSegments(
            new HudRectangle(100, 100, 80, 20), List.of(new HudRectangle(180, 120, 60, 20)));

    assertEquals(List.of(new HudOutlineSegment(99, 180)), segments.bottom());
    assertEquals(List.of(new HudOutlineSegment(99, 120)), segments.right());
  }

  @Test
  void removesPerpendicularOutlineExtensionsInsideAnOverlappingModule() {
    HudOutlineSegments segments =
        HudOutlineResolver.visibleSegments(
            new HudRectangle(100, 100, 80, 20), List.of(new HudRectangle(50, 60, 60, 41)));

    assertEquals(List.of(new HudOutlineSegment(101, 121)), segments.left());
    assertEquals(List.of(new HudOutlineSegment(110, 181)), segments.top());
  }

  @Test
  void alignedButSeparatedModulesKeepCompleteOutlines() {
    HudOutlineSegments segments =
        HudOutlineResolver.visibleSegments(
            new HudRectangle(100, 100, 80, 20), List.of(new HudRectangle(100, 300, 80, 20)));

    assertEquals(List.of(new HudOutlineSegment(99, 181)), segments.top());
    assertEquals(List.of(new HudOutlineSegment(99, 121)), segments.left());
  }
}
