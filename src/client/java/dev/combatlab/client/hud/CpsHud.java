package dev.combatlab.client.hud;

import dev.combatlab.client.config.CombatLabOptions;
import dev.combatlab.client.debug.DebugLogger;
import dev.combatlab.client.state.ClientGameState;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public final class CpsHud extends TextHudModule {
	private static final HudModuleDefinition DEFINITION = new HudModuleDefinition(
			Identifier.fromNamespaceAndPath("combatlab", "cps"),
			Component.literal("CPS HUD"),
			1.0,
			0.08,
			true
	);

	public static HudModuleDescriptor descriptor() {
		return new HudModuleDescriptor(DEFINITION, dependencies -> new CpsHud(dependencies.options(), dependencies.debug()));
	}

	public CpsHud(CombatLabOptions options, DebugLogger debug) {
		super(
				DEFINITION,
				"0 CPS",
				options,
				debug
		);
	}

	@Override
	public void tick(ClientGameState gameState) {
		setText(gameState.input().cps() + " CPS");
	}
}
