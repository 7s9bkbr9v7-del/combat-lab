package dev.combatlab.client.state;

public record InputState(int cps) {
	public static InputState empty() {
		return new InputState(0);
	}
}
