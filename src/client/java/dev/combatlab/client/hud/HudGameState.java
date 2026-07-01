package dev.combatlab.client.hud;

import dev.combatlab.client.state.ClientGameState;
import dev.combatlab.client.state.CombatSnapshot;
import dev.combatlab.client.state.InputState;
import dev.combatlab.client.state.MovementState;
import dev.combatlab.client.state.PlayerArmor;
import dev.combatlab.client.state.PlayerEffects;
import dev.combatlab.client.state.PlayerState;

public record HudGameState(
    int fps, int cps, int ping, MovementState movement, PlayerArmor armor, PlayerEffects effects) {
  public static HudGameState empty() {
    return from(0, PlayerState.absent(), InputState.empty(), CombatSnapshot.empty());
  }

  public static HudGameState from(ClientGameState state) {
    if (state == null) {
      return empty();
    }
    return from(state.fps(), state.player(), state.input(), state.combat());
  }

  public static HudGameState from(
      int fps, PlayerState player, InputState input, CombatSnapshot combat) {
    PlayerState safePlayer = player == null ? PlayerState.absent() : player;
    return new HudGameState(
        fps,
        input.cps(),
        combat.ping(),
        safePlayer.movement(),
        safePlayer.armor(),
        safePlayer.effects());
  }

  public HudGameState forEditorPreview() {
    return new HudGameState(
        fps,
        cps,
        ping,
        movement,
        PlayerArmor.editorPreview(armor),
        PlayerEffects.editorPreview(effects));
  }
}
