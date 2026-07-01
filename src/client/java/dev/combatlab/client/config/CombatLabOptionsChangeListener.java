package dev.combatlab.client.config;

public interface CombatLabOptionsChangeListener {
  default void onDebugLoggingEnabledChanged(boolean enabled) {}

  default void onFullbrightEnabledChanged(boolean enabled) {}

  default void onAchievementToastsDisabledChanged(boolean disabled) {}

  default void onDynamicFovEnabledChanged(boolean enabled) {}
}
