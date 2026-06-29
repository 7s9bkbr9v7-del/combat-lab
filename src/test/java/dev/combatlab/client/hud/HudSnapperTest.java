package dev.combatlab.client.hud;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

class HudSnapperTest {
  @Test
  void snapsAdjacentAndAlignedEdges() {
    HudRectangle stationary = new HudRectangle(100, 100, 80, 20);
    HudPosition snapped =
        HudSnapper.snap(new HudRectangle(183, 103, 60, 20), List.of(stationary), 6);

    assertEquals(new HudPosition(180, 100), snapped);
  }

  @Test
  void ignoresElementsOutsideThePerpendicularRange() {
    HudPosition snapped =
        HudSnapper.snap(
            new HudRectangle(130, 300, 60, 20), List.of(new HudRectangle(100, 100, 80, 20)), 6);

    assertEquals(new HudPosition(130, 300), snapped);
  }

  @Test
  void alignsCentersAcrossTheScreen() {
    HudPosition snapped =
        HudSnapper.snap(
            new HudRectangle(109, 300, 60, 20), List.of(new HudRectangle(100, 100, 80, 20)), 6);

    assertEquals(new HudPosition(110, 300), snapped);
  }

  @Test
  void snapsToScreenEdgesAndCorners() {
    HudPosition left = HudSnapper.snap(new HudRectangle(5, 40, 60, 20), List.of(), 6, 300, 200);
    assertEquals(new HudPosition(0, 40), left);

    HudPosition bottomRight =
        HudSnapper.snap(new HudRectangle(236, 177, 60, 20), List.of(), 6, 300, 200);
    assertEquals(new HudPosition(240, 180), bottomRight);
  }

  @Test
  void snapsToHorizontalAndVerticalScreenCentersIndependently() {
    HudPosition horizontalCenter =
        HudSnapper.snap(new HudRectangle(124, 30, 60, 20), List.of(), 6, 300, 200);
    assertEquals(new HudPosition(120, 30), horizontalCenter);

    HudPosition verticalCenter =
        HudSnapper.snap(new HudRectangle(30, 86, 60, 20), List.of(), 6, 300, 200);
    assertEquals(new HudPosition(30, 90), verticalCenter);
  }

  @Test
  void snapsToNearestGridLineWithinScreenBounds() {
    HudPosition snapped = HudSnapper.snapToGrid(new HudRectangle(13, 18, 20, 10), 4, 100, 80);

    assertEquals(new HudPosition(12, 20), snapped);
  }

  @Test
  void gridSnapClampsToScreenBounds() {
    HudPosition snapped = HudSnapper.snapToGrid(new HudRectangle(98, 79, 20, 10), 4, 100, 80);

    assertEquals(new HudPosition(80, 70), snapped);
  }

  @Test
  void createsGuideForScreenCenterAlignment() {
    HudSnapResult result =
        HudSnapper.snapWithGuides(new HudRectangle(121, 30, 60, 20), List.of(), 6, 300, 200);

    assertEquals(new HudPosition(120, 30), result.position());
    assertEquals(List.of(new HudSnapGuide(HudSnapGuide.Axis.VERTICAL, 150)), result.guides());
  }

  @Test
  void createsBothGuidesForScreenCenterAlignment() {
    List<HudSnapGuide> guides =
        HudSnapper.guidesForAlignment(
            new HudRectangle(121, 91, 60, 20),
            new HudRectangle(120, 90, 60, 20),
            List.of(),
            6,
            300,
            200);

    assertEquals(
        List.of(
            new HudSnapGuide(HudSnapGuide.Axis.VERTICAL, 150),
            new HudSnapGuide(HudSnapGuide.Axis.HORIZONTAL, 100)),
        guides);
  }

  @Test
  void createsGuideForDistantModuleEdgeAlignment() {
    List<HudSnapGuide> guides =
        HudSnapper.guidesForAlignment(
            new HudRectangle(102, 300, 60, 20),
            new HudRectangle(100, 300, 60, 20),
            List.of(new HudRectangle(100, 100, 80, 20)),
            6,
            300,
            400);

    assertEquals(List.of(new HudSnapGuide(HudSnapGuide.Axis.VERTICAL, 100)), guides);
  }

  @Test
  void createsGuideForDistantOppositeModuleEdgeAlignment() {
    List<HudSnapGuide> guides =
        HudSnapper.guidesForAlignment(
            new HudRectangle(179, 300, 60, 20),
            new HudRectangle(180, 300, 60, 20),
            List.of(new HudRectangle(100, 100, 80, 20)),
            6,
            300,
            400);

    assertEquals(List.of(new HudSnapGuide(HudSnapGuide.Axis.VERTICAL, 180)), guides);
  }

  @Test
  void snapsDistantLeftEdgeToOtherRightEdge() {
    HudPosition snapped =
        HudSnapper.snap(
            new HudRectangle(179, 300, 60, 20), List.of(new HudRectangle(100, 100, 80, 20)), 6);

    assertEquals(new HudPosition(180, 300), snapped);
  }

  @Test
  void snapsDistantRightEdgeToOtherLeftEdge() {
    HudPosition snapped =
        HudSnapper.snap(
            new HudRectangle(39, 300, 60, 20), List.of(new HudRectangle(100, 100, 80, 20)), 6);

    assertEquals(new HudPosition(40, 300), snapped);
  }

  @Test
  void createsGuideForDistantOppositeHorizontalModuleEdgeAlignment() {
    List<HudSnapGuide> guides =
        HudSnapper.guidesForAlignment(
            new HudRectangle(250, 119, 60, 20),
            new HudRectangle(250, 120, 60, 20),
            List.of(new HudRectangle(100, 100, 80, 20)),
            6,
            400,
            300);

    assertEquals(List.of(new HudSnapGuide(HudSnapGuide.Axis.HORIZONTAL, 120)), guides);
  }

  @Test
  void snapsDistantTopEdgeToOtherBottomEdge() {
    HudPosition snapped =
        HudSnapper.snap(
            new HudRectangle(250, 119, 60, 20), List.of(new HudRectangle(100, 100, 80, 20)), 6);

    assertEquals(new HudPosition(250, 120), snapped);
  }

  @Test
  void snapsDistantBottomEdgeToOtherTopEdge() {
    HudPosition snapped =
        HudSnapper.snap(
            new HudRectangle(250, 79, 60, 20), List.of(new HudRectangle(100, 100, 80, 20)), 6);

    assertEquals(new HudPosition(250, 80), snapped);
  }

  @Test
  void keepsDistantGuideWhenAlsoAdjacentToAnotherModule() {
    List<HudSnapGuide> guides =
        HudSnapper.guidesForAlignment(
            new HudRectangle(102, 180, 60, 20),
            new HudRectangle(100, 180, 60, 20),
            List.of(new HudRectangle(100, 100, 80, 20), new HudRectangle(160, 180, 80, 20)),
            6,
            300,
            400);

    assertEquals(List.of(new HudSnapGuide(HudSnapGuide.Axis.VERTICAL, 100)), guides);
  }

  @Test
  void skipsGuideForNearbyModuleSnaps() {
    List<HudSnapGuide> guides =
        HudSnapper.guidesForAlignment(
            new HudRectangle(183, 103, 60, 20),
            new HudRectangle(180, 100, 60, 20),
            List.of(new HudRectangle(100, 100, 80, 20)),
            6,
            300,
            400);

    assertEquals(List.of(), guides);
  }
}
