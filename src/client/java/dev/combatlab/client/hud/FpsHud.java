package dev.combatlab.client.hud;

import dev.combatlab.client.config.CombatLabOptions;
import dev.combatlab.client.debug.DebugLogger;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public final class FpsHud extends TextHudModule {
	public FpsHud(CombatLabOptions options, DebugLogger debug) {
		super(
				Identifier.fromNamespaceAndPath("combatlab", "fps"),
				Component.literal("FPS HUD"),
				"-- FPS",
				1.0,
				0.02,
				options,
				debug
		);
	}

	@Override
	public void tick() {
		setText(Minecraft.getInstance().getFps() + " FPS");
	}
}
