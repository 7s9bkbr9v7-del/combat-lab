package dev.combatlab.client.hud;

import dev.combatlab.client.config.CombatLabOptions;
import dev.combatlab.client.debug.DebugLogger;
import dev.combatlab.client.input.CpsTracker;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public final class CpsHud extends TextHudModule {
	private final CpsTracker tracker;

	public CpsHud(CpsTracker tracker, CombatLabOptions options, DebugLogger debug) {
		super(
				Identifier.fromNamespaceAndPath("combatlab", "cps"),
				Component.literal("CPS HUD"),
				"0 CPS",
				1.0,
				0.08,
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
