package dev.combatlab.client.screen.hudeditor;

import dev.combatlab.client.hud.HudAttachmentSide;
import dev.combatlab.client.hud.HudModule;
import dev.combatlab.client.hud.HudRectangle;
import java.util.List;

final class HudModuleAttachment {
  private HudModuleAttachment() {}

  static boolean updateAttachment(
      HudSelection selection,
      HudModule movingModule,
      HudRectangle moving,
      List<HudSelection.ModuleRectangle> others,
      int threshold) {
    movingModule.clearAttachment();
    for (HudSelection.ModuleRectangle candidate : others) {
      HudRectangle target = candidate.rectangle();
      if (!selection.canAttach(movingModule, candidate.module())) {
        continue;
      }
      if (verticalRangesNear(moving, target, threshold)) {
        if (moving.right() == target.x()) {
          movingModule.attachTo(
              candidate.module(), HudAttachmentSide.LEFT_OF, moving.y() - target.y());
          return true;
        }
        if (moving.x() == target.right()) {
          movingModule.attachTo(
              candidate.module(), HudAttachmentSide.RIGHT_OF, moving.y() - target.y());
          return true;
        }
      }
      if (horizontalRangesNear(moving, target, threshold)) {
        if (moving.bottom() == target.y()) {
          movingModule.attachTo(
              candidate.module(), HudAttachmentSide.ABOVE, moving.x() - target.x());
          return true;
        }
        if (moving.y() == target.bottom()) {
          movingModule.attachTo(
              candidate.module(), HudAttachmentSide.BELOW, moving.x() - target.x());
          return true;
        }
      }
    }
    return false;
  }

  static boolean placeAndAttach(
      HudSelection selection,
      HudModule movingModule,
      HudRectangle moving,
      List<HudSelection.ModuleRectangle> others,
      int threshold,
      int screenWidth,
      int screenHeight) {
    movingModule.updatePosition(moving.x(), moving.y(), screenWidth, screenHeight);
    return updateAttachment(selection, movingModule, moving, others, threshold);
  }

  private static boolean verticalRangesNear(
      HudRectangle first, HudRectangle second, int threshold) {
    return first.bottom() + threshold >= second.y() && second.bottom() + threshold >= first.y();
  }

  private static boolean horizontalRangesNear(
      HudRectangle first, HudRectangle second, int threshold) {
    return first.right() + threshold >= second.x() && second.right() + threshold >= first.x();
  }
}
