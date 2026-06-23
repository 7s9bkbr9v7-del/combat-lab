package dev.combatlab.client.external;

public record ExternalTelemetrySnapshot(
		int schemaVersion,
		int fps,
		int cps,
		int ping,
		float attackStrength,
		ExternalTargetSnapshot target
) {
}
