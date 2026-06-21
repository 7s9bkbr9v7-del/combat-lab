package dev.combatlab.client.hud;

public final class MovementStatusText {
	private MovementStatusText() {
	}

	public static String resolve(boolean crouched, boolean sprinting, boolean toggleSprint) {
		if (crouched) {
			return "Crouched";
		}
		if (sprinting) {
			return toggleSprint ? "Sprinting (Toggled)" : "Sprinting";
		}
		return "";
	}
}
