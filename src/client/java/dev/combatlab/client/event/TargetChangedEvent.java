package dev.combatlab.client.event;

import java.util.UUID;

public record TargetChangedEvent(
		UUID previousTargetId,
		String previousTargetName,
		UUID targetId,
		String targetName,
		float targetDistance,
		long capturedAtNanos
) implements CombatEvent {
	public boolean hasTarget() {
		return targetId != null;
	}
}
