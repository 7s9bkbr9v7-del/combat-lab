package dev.combatlab.client.hud;

import dev.combatlab.client.config.CombatLabOptions;
import dev.combatlab.client.debug.DebugLogger;
import java.util.Objects;

public record HudModuleDependencies(
    CombatLabOptions options,
    DebugLogger debug,
    StatusEffectVisibilityPolicy statusEffectVisibilityPolicy) {
  public HudModuleDependencies {
    Objects.requireNonNull(options, "options");
    Objects.requireNonNull(debug, "debug");
    Objects.requireNonNull(statusEffectVisibilityPolicy, "statusEffectVisibilityPolicy");
  }
}
