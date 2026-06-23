package dev.combatlab.client.state;

public record MovementState(
		boolean crouching,
		boolean sprinting,
		boolean sprintToggled
) {
	public static MovementState inactive() {
		return new MovementState(false, false, false);
	}
}
