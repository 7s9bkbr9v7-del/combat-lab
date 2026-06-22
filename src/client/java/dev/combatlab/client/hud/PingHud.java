package dev.combatlab.client.hud;

import dev.combatlab.client.config.CombatLabOptions;
import dev.combatlab.client.debug.DebugLogger;
import dev.combatlab.client.model.CombatState;
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

	private final CombatState combatState;

	public PingHud(CombatState combatState, CombatLabOptions options, DebugLogger debug) {
		super(DEFINITION, "-- ms", options, debug);
		this.combatState = combatState;
	}

	@Override
	public void tick() {
		setText(PingText.resolve(combatState.ping()));
	}
}
