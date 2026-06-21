package dev.combatlab.client.debug;

import dev.combatlab.client.CombatLabClient;

import java.util.function.BooleanSupplier;

public final class DebugLogger {
	private final BooleanSupplier enabled;

	public DebugLogger(BooleanSupplier enabled) {
		this.enabled = enabled;
	}

	public void info(String message, Object... arguments) {
		if (isEnabled()) {
			CombatLabClient.LOGGER.info("[debug] " + message, arguments);
		}
	}

	public boolean isEnabled() {
		return enabled.getAsBoolean();
	}

	public void announce(boolean nowEnabled) {
		CombatLabClient.LOGGER.info("[debug] Debug logging {}", nowEnabled ? "enabled" : "disabled");
	}
}
