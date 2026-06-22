package dev.combatlab.client.feature;

public final class DynamicFovController {
	private static boolean enabled = true;

	private DynamicFovController() {
	}

	public static boolean enabled() {
		return enabled;
	}

	public static void setEnabled(boolean enabled) {
		DynamicFovController.enabled = enabled;
	}
}
