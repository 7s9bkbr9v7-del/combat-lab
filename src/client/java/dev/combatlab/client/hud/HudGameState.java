package dev.combatlab.client.hud;

import dev.combatlab.client.state.CombatSnapshot;
import dev.combatlab.client.state.InputState;
import dev.combatlab.client.state.MovementState;
import dev.combatlab.client.state.PlayerArmor;
import dev.combatlab.client.state.PlayerState;

public record HudGameState(
		int fps,
		int cps,
		int ping,
		MovementState movement,
		PlayerArmor armor
) {
	public static HudGameState empty() {
		return from(0, PlayerState.absent(), InputState.empty(), CombatSnapshot.empty());
	}

	public static HudGameState from(int fps, PlayerState player, InputState input, CombatSnapshot combat) {
		return new HudGameState(
				fps,
				input.cps(),
				combat.ping(),
				player.movement(),
				player.armor()
		);
	}

	public HudGameState forEditorPreview() {
		return new HudGameState(
				fps,
				cps,
				ping,
				movement,
				PlayerArmor.editorPreview(armor)
		);
	}
}
