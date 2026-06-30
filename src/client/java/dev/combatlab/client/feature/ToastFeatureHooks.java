package dev.combatlab.client.feature;

import net.minecraft.client.gui.components.toasts.AdvancementToast;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.TutorialToast;

public final class ToastFeatureHooks {
  private ToastFeatureHooks() {}

  public static boolean shouldSuppress(Toast toast) {
    return AchievementToastController.disabled()
        && (toast instanceof AdvancementToast || toast instanceof TutorialToast);
  }
}
