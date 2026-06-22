package dev.combatlab.client.hud;

public final class MovementStatusText {
	private MovementStatusText() {
	}

	public static String resolve(boolean crouched, boolean sprinting, boolean toggleSprintActive) {
		if (crouched) {
			return "Crouched";
		}
		if (toggleSprintActive) {
			return "Sprinting (Toggled)";
		}
		if (sprinting) {
			return "Sprinting";
		}
		return "";
	}
}
