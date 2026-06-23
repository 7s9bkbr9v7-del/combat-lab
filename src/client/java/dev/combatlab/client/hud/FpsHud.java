package dev.combatlab.client.hud;

import dev.combatlab.client.config.CombatLabOptions;
import dev.combatlab.client.debug.DebugLogger;
import dev.combatlab.client.state.ClientGameState;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public final class FpsHud extends TextHudModule {
	private static final HudModuleDefinition DEFINITION = new HudModuleDefinition(
			Identifier.fromNamespaceAndPath("combatlab", "fps"),
			Component.literal("FPS HUD"),
			1.0,
			0.02,
			true
	);

	public FpsHud(CombatLabOptions options, DebugLogger debug) {
		super(
				DEFINITION,
				"-- FPS",
				options,
				debug
		);
	}

	@Override
	public void tick(ClientGameState gameState) {
		setText(gameState.hud().fps() + " FPS");
	}
}
