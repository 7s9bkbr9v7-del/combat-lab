package dev.combatlab.client.state;

public record PlayerState(
		boolean present,
		MovementState movement,
		PlayerArmor armor
) {
	public static PlayerState absent() {
		return new PlayerState(false, MovementState.inactive(), PlayerArmor.empty());
	}
}
