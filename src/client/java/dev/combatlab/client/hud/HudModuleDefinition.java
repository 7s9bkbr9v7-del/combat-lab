package dev.combatlab.client.hud;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.Objects;

public record HudModuleDefinition(
		Identifier id,
		Component displayName,
		double defaultX,
		double defaultY,
		boolean resizable
) {
	public HudModuleDefinition {
		Objects.requireNonNull(id, "id");
		Objects.requireNonNull(displayName, "displayName");
		if (defaultX < 0.0 || defaultX > 1.0 || defaultY < 0.0 || defaultY > 1.0) {
			throw new IllegalArgumentException("Default HUD position must be normalized");
		}
	}
}
