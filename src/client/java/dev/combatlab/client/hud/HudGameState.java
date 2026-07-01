package dev.combatlab.client.hud;

import dev.combatlab.client.state.CombatSnapshot;
import dev.combatlab.client.state.DirectionState;
import dev.combatlab.client.state.InputState;
import dev.combatlab.client.state.MovementState;
import dev.combatlab.client.state.PlayerArmor;
import dev.combatlab.client.state.PlayerEffects;
import dev.combatlab.client.state.PlayerState;

public record HudGameState(
    int fps,
    int cps,
    int ping,
    InputState input,
    DirectionState direction,
    MovementState movement,
    PlayerArmor armor,
    PlayerEffects effects) {
  public static HudGameState empty() {
    return from(0, PlayerState.absent(), InputState.empty(), CombatSnapshot.empty());
  }

  public static HudGameState from(
      int fps, PlayerState player, InputState input, CombatSnapshot combat) {
    PlayerState safePlayer = player == null ? PlayerState.absent() : player;
    InputState safeInput = input == null ? InputState.empty() : input;
    CombatSnapshot safeCombat = combat == null ? CombatSnapshot.empty() : combat;
    return new HudGameState(
        fps,
        safeInput.cps(),
        safeCombat.ping(),
        safeInput,
        safePlayer.direction(),
        safePlayer.movement(),
        safePlayer.armor(),
        safePlayer.effects());
  }

  public HudGameState forEditorPreview() {
    return new HudGameState(
        fps,
        cps,
        ping,
        input,
        direction,
        movement,
        PlayerArmor.editorPreview(armor),
        PlayerEffects.editorPreview(effects));
  }
}
