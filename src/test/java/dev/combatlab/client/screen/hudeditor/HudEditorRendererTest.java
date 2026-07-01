package dev.combatlab.client.screen.hudeditor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import dev.combatlab.client.hud.HudRectangle;
import dev.combatlab.client.hud.HudSnapGuide;
import org.junit.jupiter.api.Test;

class HudEditorRendererTest {
  @Test
  void snapGuideLinesAreInsetFromScreenEdges() {
    assertEquals(
        new HudRectangle(50, 1, 1, 98),
        HudEditorRenderer.snapGuideLineBounds(
            new HudSnapGuide(HudSnapGuide.Axis.VERTICAL, 50), 100, 100));

    assertEquals(
        new HudRectangle(1, 60, 98, 1),
        HudEditorRenderer.snapGuideLineBounds(
            new HudSnapGuide(HudSnapGuide.Axis.HORIZONTAL, 60), 100, 100));
  }

  @Test
  void snapGuideLinesDoNotDrawOnScreenEdges() {
    assertNull(
        HudEditorRenderer.snapGuideLineBounds(
            new HudSnapGuide(HudSnapGuide.Axis.VERTICAL, 0), 100, 100));
    assertNull(
        HudEditorRenderer.snapGuideLineBounds(
            new HudSnapGuide(HudSnapGuide.Axis.VERTICAL, 99), 100, 100));
    assertNull(
        HudEditorRenderer.snapGuideLineBounds(
            new HudSnapGuide(HudSnapGuide.Axis.HORIZONTAL, 0), 100, 100));
    assertNull(
        HudEditorRenderer.snapGuideLineBounds(
            new HudSnapGuide(HudSnapGuide.Axis.HORIZONTAL, 99), 100, 100));
  }
}
