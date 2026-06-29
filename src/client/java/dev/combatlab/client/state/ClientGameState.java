package dev.combatlab.client.state;

import dev.combatlab.client.hud.HudGameState;

public record ClientGameState(
    HudGameState hud, PlayerState player, InputState input, CombatSnapshot combat, int fps) {
  public static ClientGameState empty() {
    PlayerState player = PlayerState.absent();
    InputState input = InputState.empty();
    CombatSnapshot combat = CombatSnapshot.empty();
    int fps = 0;
    return new ClientGameState(
        HudGameState.from(fps, player, input, combat), player, input, combat, fps);
  }

  public ClientGameState withHud(HudGameState hud) {
    return new ClientGameState(hud, player, input, combat, fps);
  }
}
