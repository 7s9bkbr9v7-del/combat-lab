package dev.combatlab.client.hud;

import dev.combatlab.client.config.CombatLabOptions;
import dev.combatlab.client.debug.DebugLogger;

public record HudModuleDependencies(
		CombatLabOptions options,
		DebugLogger debug
) {
}
