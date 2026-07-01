package dev.combatlab.client.feature;

import dev.combatlab.client.config.CombatLabOptions;
import dev.combatlab.client.config.CombatLabOptionsChangeListener;
import dev.combatlab.client.debug.DebugLogger;

public final class CombatLabOptionsApplier implements CombatLabOptionsChangeListener {
  private final DebugLogger debug;

  public CombatLabOptionsApplier(DebugLogger debug) {
    this.debug = debug;
  }

  public void applyStartupOptions(CombatLabOptions options) {
    FullbrightController.setEnabled(options.fullbrightEnabled());
    AchievementToastController.setDisabled(options.achievementToastsDisabled());
    DynamicFovController.setEnabled(options.dynamicFovEnabled());
  }

  @Override
  public void onDebugLoggingEnabledChanged(boolean enabled) {
    debug.announce(enabled);
  }

  @Override
  public void onFullbrightEnabledChanged(boolean enabled) {
    FullbrightController.setEnabled(enabled);
    debug.info("Fullbright {}", enabled ? "enabled" : "disabled");
  }

  @Override
  public void onAchievementToastsDisabledChanged(boolean disabled) {
    AchievementToastController.setDisabled(disabled);
    debug.info("Achievement notifications {}", disabled ? "disabled" : "enabled");
  }

  @Override
  public void onDynamicFovEnabledChanged(boolean enabled) {
    DynamicFovController.setEnabled(enabled);
    debug.info("Dynamic FOV {}", enabled ? "enabled" : "disabled");
  }
}
