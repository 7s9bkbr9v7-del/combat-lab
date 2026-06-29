package dev.combatlab.client.state;

public record PlayerState(
    boolean present,
    DirectionState direction,
    MovementState movement,
    PlayerArmor armor,
    PlayerEffects effects) {
  public static PlayerState absent() {
    return new PlayerState(
        false,
        DirectionState.absent(),
        MovementState.inactive(),
        PlayerArmor.empty(),
        PlayerEffects.empty());
  }
}
