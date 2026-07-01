package dev.combatlab.client.state;

public record ClientGameState(
    PlayerState player, InputState input, CombatSnapshot combat, int fps) {
  public static ClientGameState empty() {
    PlayerState player = PlayerState.absent();
    InputState input = InputState.empty();
    CombatSnapshot combat = CombatSnapshot.empty();
    int fps = 0;
    return new ClientGameState(player, input, combat, fps);
  }
}
