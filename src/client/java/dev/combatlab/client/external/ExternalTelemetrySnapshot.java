package dev.combatlab.client.external;

import java.util.List;

public record ExternalTelemetrySnapshot(
		int schemaVersion,
		int fps,
		int cps,
		int ping,
		float attackStrength,
		ExternalInputSnapshot input,
		ExternalTargetSnapshot target,
		List<ExternalEffectTimer> effects
) {
}
