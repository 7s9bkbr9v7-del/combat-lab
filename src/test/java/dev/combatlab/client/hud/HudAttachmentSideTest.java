package dev.combatlab.client.hud;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class HudAttachmentSideTest {
  @Test
  void keepsAttachedEdgeAndOffsetWhenTargetMoves() {
    HudSize attachedSize = new HudSize(40, 12);

    HudPosition initial =
        HudAttachmentSide.RIGHT_OF.resolve(new HudRectangle(100, 50, 60, 20), attachedSize, 3);
    HudPosition afterGuiScaleChange =
        HudAttachmentSide.RIGHT_OF.resolve(new HudRectangle(220, 90, 60, 20), attachedSize, 3);

    assertEquals(new HudPosition(160, 53), initial);
    assertEquals(new HudPosition(280, 93), afterGuiScaleChange);
  }

  @Test
  void stacksVerticalAttachmentsWithoutAGap() {
    HudPosition below =
        HudAttachmentSide.BELOW.resolve(new HudRectangle(40, 30, 80, 18), new HudSize(50, 12), 5);

    assertEquals(new HudPosition(45, 48), below);
  }

  @Test
  void clampsOffsetsSoAttachedModulesStayTouchingTarget() {
    HudRectangle target = new HudRectangle(40, 30, 80, 18);

    assertEquals(
        new HudPosition(120, 48),
        HudAttachmentSide.RIGHT_OF.resolve(target, new HudSize(50, 12), 66));
    assertEquals(
        new HudPosition(120, 18),
        HudAttachmentSide.RIGHT_OF.resolve(target, new HudSize(50, 12), -30));
    assertEquals(
        new HudPosition(120, 48),
        HudAttachmentSide.BELOW.resolve(target, new HudSize(50, 12), 200));
    assertEquals(
        new HudPosition(-10, 48),
        HudAttachmentSide.BELOW.resolve(target, new HudSize(50, 12), -90));
  }
}
