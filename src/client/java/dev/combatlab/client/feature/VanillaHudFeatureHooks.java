package dev.combatlab.client.feature;

import dev.combatlab.client.compat.CompatFeatureSwitch;

public final class VanillaHudFeatureHooks {
	private static final String EFFECT_TIMERS_MODULE_ID = "combatlab:effects";
	private static final CompatFeatureSwitch SUPPRESS_STATUS_EFFECTS_HUD = CompatFeatureSwitch.initiallyDisabled();

	private VanillaHudFeatureHooks() {
	}

	public static void updateHudModuleState(String id, boolean enabled) {
		if (EFFECT_TIMERS_MODULE_ID.equals(id)) {
			SUPPRESS_STATUS_EFFECTS_HUD.setEnabled(enabled);
		}
	}

	public static boolean shouldSuppressStatusEffectsHud() {
		return SUPPRESS_STATUS_EFFECTS_HUD.enabled();
	}
}
