package dev.combatlab.client.state;

public record InputState(
		int cps,
		boolean forward,
		boolean left,
		boolean back,
		boolean right,
		boolean jump,
		boolean sneak,
		boolean sprint,
		boolean attack,
		boolean use
) {
	public InputState(int cps) {
		this(cps, false, false, false, false, false, false, false, false, false);
	}

	public static InputState empty() {
		return new InputState(0);
	}

	public boolean anyMovementKeyDown() {
		return forward || left || back || right || jump || sneak || sprint;
	}

	public boolean anyMouseButtonDown() {
		return attack || use;
	}
}
