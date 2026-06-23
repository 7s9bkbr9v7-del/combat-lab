package dev.combatlab.client.hud;

import java.util.Objects;

public record HudModuleDescriptor(
		HudModuleDefinition definition,
		HudModuleFactory factory,
		boolean loadWhenDisabled
) {
	public HudModuleDescriptor(HudModuleDefinition definition, HudModuleFactory factory) {
		this(definition, factory, false);
	}

	public HudModuleDescriptor {
		Objects.requireNonNull(definition, "definition");
		Objects.requireNonNull(factory, "factory");
	}

	public String id() {
		return definition.id().toString();
	}
}
