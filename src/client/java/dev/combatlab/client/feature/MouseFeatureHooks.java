package dev.combatlab.client.feature;

import net.minecraft.client.player.LocalPlayer;

public final class MouseFeatureHooks {
  private MouseFeatureHooks() {}

  public static void turnPlayerOrFreelook(LocalPlayer player, double deltaX, double deltaY) {
    double scaledDeltaX = deltaX * ZoomController.mouseSensitivityScale();
    double scaledDeltaY = deltaY * ZoomController.mouseSensitivityScale();
    if (FreelookController.active()) {
      FreelookController.turn(scaledDeltaX, scaledDeltaY);
    } else {
      player.turn(scaledDeltaX, scaledDeltaY);
    }
  }
}
