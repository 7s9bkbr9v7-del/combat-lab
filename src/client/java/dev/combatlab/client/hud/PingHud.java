package dev.combatlab.client.hud;

import dev.combatlab.client.config.CombatLabOptions;
import dev.combatlab.client.debug.DebugLogger;
import dev.combatlab.client.state.ClientGameState;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public final class PingHud extends TextHudModule {
	private static final HudModuleDefinition DEFINITION = new HudModuleDefinition(
			Identifier.fromNamespaceAndPath("combatlab", "ping"),
			Component.literal("Ping HUD"),
			1.0,
			0.20,
			true
	);

	public PingHud(CombatLabOptions options, DebugLogger debug) {
		super(DEFINITION, "-- ms", options, debug);
	}

	@Override
	public void tick(ClientGameState gameState) {
		setText(PingText.resolve(gameState.combat().ping()));
	}
}
