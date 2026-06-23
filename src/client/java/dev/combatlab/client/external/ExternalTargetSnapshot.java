package dev.combatlab.client.external;

public record ExternalTargetSnapshot(
		boolean present,
		String id,
		String name,
		float distance
) {
}
