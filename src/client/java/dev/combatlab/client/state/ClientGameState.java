package dev.combatlab.client.state;

public record ClientGameState(InputState input, CombatSnapshot combat, int fps) {
  public static ClientGameState empty() {
    InputState input = InputState.empty();
    CombatSnapshot combat = CombatSnapshot.empty();
    int fps = 0;
    return new ClientGameState(input, combat, fps);
  }
}
