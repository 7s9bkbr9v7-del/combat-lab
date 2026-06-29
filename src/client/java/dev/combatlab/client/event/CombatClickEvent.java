package dev.combatlab.client.event;

public record CombatClickEvent(int clickCount, long capturedAtNanos) implements CombatEvent {
  public CombatClickEvent {
    if (clickCount < 1) {
      throw new IllegalArgumentException("clickCount must be positive");
    }
  }
}
