package dev.combatlab.client.debug;

import dev.combatlab.client.model.CombatState;

import java.util.Objects;

/**
 * Emits state transitions only. Rendering and tick loops deliberately stay quiet.
 */
public final class DebugTelemetry {
	private String previousTarget;
	private boolean previouslyEnabled;

	public void update(CombatState state, boolean enabled, DebugLogger debug) {
		if (!enabled) {
			previousTarget = null;
			previouslyEnabled = false;
			return;
		}

		String target = state.targetName();
		if (!previouslyEnabled) {
			previousTarget = target;
			previouslyEnabled = true;
			return;
		}

		if (!Objects.equals(previousTarget, target)) {
			if (target == null) {
				debug.info("Crosshair target cleared (was {})", previousTarget);
			} else {
				debug.info("Crosshair target: {} at {} blocks", target, String.format("%.2f", state.targetDistance()));
			}
			previousTarget = target;
		}
	}
}
