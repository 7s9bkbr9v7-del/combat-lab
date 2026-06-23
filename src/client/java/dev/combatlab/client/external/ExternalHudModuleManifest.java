package dev.combatlab.client.external;

import java.util.List;

public record ExternalHudModuleManifest(
		int schemaVersion,
		List<ExternalHudModuleDefinition> modules
) {
}
