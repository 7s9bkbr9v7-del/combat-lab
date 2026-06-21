package dev.combatlab.client.model;

import java.util.UUID;

public record AttackEvent(
		long sequence,
		long gameTick,
		long capturedAtNanos,
		UUID targetId,
		String targetName,
		float targetDistance,
		float attackStrength,
		int ping
) {
	public boolean hasTarget() {
		return targetId != null;
	}
}
