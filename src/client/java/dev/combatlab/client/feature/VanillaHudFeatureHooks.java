package dev.combatlab.client.feature;

import dev.combatlab.client.compat.CompatFeatureSwitch;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.Minecraft;
import net.minecraft.world.effect.MobEffectInstance;

public final class VanillaHudFeatureHooks {
	private static final String EFFECT_TIMERS_MODULE_ID = "combatlab:effects";
	private static final CompatFeatureSwitch SUPPRESS_STATUS_EFFECTS_HUD = CompatFeatureSwitch.initiallyDisabled();
	private static boolean installed;

	private VanillaHudFeatureHooks() {
	}

	public static void updateHudModuleState(String id, boolean enabled) {
		if (EFFECT_TIMERS_MODULE_ID.equals(id)) {
			SUPPRESS_STATUS_EFFECTS_HUD.setEnabled(enabled);
		}
	}

	public static boolean shouldSuppressStatusEffectsHud() {
		return SUPPRESS_STATUS_EFFECTS_HUD.enabled() || fullbrightNightVisionActive();
	}

	public static void installStatusEffectsHudSuppression() {
		if (installed) {
			return;
		}
		HudElementRegistry.replaceElement(VanillaHudElements.MOB_EFFECTS, vanilla -> (graphics, deltaTracker) -> {
			if (!shouldSuppressStatusEffectsHud()) {
				vanilla.extractRenderState(graphics, deltaTracker);
			}
		});
		installed = true;
	}

	private static boolean fullbrightNightVisionActive() {
		if (!FullbrightController.enabled()) {
			return false;
		}
		Minecraft client = Minecraft.getInstance();
		if (client.player == null) {
			return false;
		}
		return client.player.getActiveEffects().stream()
				.filter(MobEffectInstance::showIcon)
				.anyMatch(FullbrightController::shouldHideEffectStatus);
	}
}
