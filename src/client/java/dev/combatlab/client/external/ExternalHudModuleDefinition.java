package dev.combatlab.client.external;

public record ExternalHudModuleDefinition(
		String id,
		String displayName,
		double defaultX,
		double defaultY,
		boolean resizable,
		boolean loadWhenDisabled
) {
}
