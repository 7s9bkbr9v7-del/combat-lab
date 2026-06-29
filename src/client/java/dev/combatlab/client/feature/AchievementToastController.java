package dev.combatlab.client.feature;

public final class AchievementToastController {
  private static boolean disabled;

  private AchievementToastController() {}

  public static boolean disabled() {
    return disabled;
  }

  public static void setDisabled(boolean disabled) {
    AchievementToastController.disabled = disabled;
  }
}
