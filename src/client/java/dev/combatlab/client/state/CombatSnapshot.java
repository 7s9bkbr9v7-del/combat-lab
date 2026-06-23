package dev.combatlab.client.state;

public record CombatSnapshot(
		float attackStrength,
		int ping,
		TargetState target
) {
	public static CombatSnapshot empty() {
		return new CombatSnapshot(0.0F, -1, TargetState.none());
	}
}
