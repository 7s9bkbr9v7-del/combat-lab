package dev.combatlab.client.feature;

public final class FullbrightController {
	private static boolean enabled;

	private FullbrightController() {
	}

	public static boolean enabled() {
		return enabled;
	}

	public static void setEnabled(boolean enabled) {
		FullbrightController.enabled = enabled;
	}
}
