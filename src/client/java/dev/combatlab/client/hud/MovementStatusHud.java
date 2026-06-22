package dev.combatlab.client.hud;

import dev.combatlab.client.config.CombatLabOptions;
import dev.combatlab.client.debug.DebugLogger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public final class MovementStatusHud extends TextHudModule {
	private static final HudModuleDefinition DEFINITION = new HudModuleDefinition(
			Identifier.fromNamespaceAndPath("combatlab", "movement_status"),
			Component.literal("Movement status HUD"),
			1.0,
			0.14,
			true
	);

	private boolean active;

	public MovementStatusHud(CombatLabOptions options, DebugLogger debug) {
		super(
				DEFINITION,
				"Movement status",
				options,
				debug
		);
	}

	@Override
	public void tick() {
		Minecraft client = Minecraft.getInstance();
		LocalPlayer player = client.player;
		if (player == null) {
			active = false;
			setText("Movement status");
			return;
		}

		String status = MovementStatusText.resolve(
				player.isCrouching(),
				player.isSprinting(),
				client.options.toggleSprint().get() && client.options.keySprint.isDown()
		);
		active = !status.isEmpty();
		setText(active ? status : "Movement status");
	}

	@Override
	protected boolean shouldRenderInGame(HudRenderContext context) {
		return active;
	}
}
