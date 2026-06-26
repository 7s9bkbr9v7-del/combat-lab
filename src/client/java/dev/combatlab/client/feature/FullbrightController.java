package dev.combatlab.client.feature;

import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffectInstance;

public final class FullbrightController {
	public static final String NIGHT_VISION_EFFECT_ID = "minecraft:night_vision";
	private static boolean enabled;

	private FullbrightController() {
	}

	public static boolean enabled() {
		return enabled;
	}

	public static void setEnabled(boolean enabled) {
		FullbrightController.enabled = enabled;
	}

	public static boolean shouldHideEffectStatus(String effectId) {
		return enabled && NIGHT_VISION_EFFECT_ID.equals(effectId);
	}

	public static boolean shouldHideEffectStatus(MobEffectInstance effect) {
		return shouldHideEffectStatus(effectId(effect));
	}

	private static String effectId(MobEffectInstance effect) {
		Identifier identifier = effect.getEffect().unwrapKey()
				.map(key -> key.identifier())
				.orElse(null);
		return identifier == null ? "" : identifier.toString();
	}
}
