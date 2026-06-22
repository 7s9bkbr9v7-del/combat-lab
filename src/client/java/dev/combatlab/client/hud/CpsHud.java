package dev.combatlab.client.hud;

import dev.combatlab.client.config.CombatLabOptions;
import dev.combatlab.client.debug.DebugLogger;
import dev.combatlab.client.input.CpsTracker;
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

	private final CpsTracker tracker;

	public CpsHud(CpsTracker tracker, CombatLabOptions options, DebugLogger debug) {
		super(
				DEFINITION,
				"0 CPS",
				options,
				debug
		);
		this.tracker = tracker;
	}

	@Override
	public void tick() {
		setText(tracker.currentCps(System.nanoTime()) + " CPS");
	}
}
