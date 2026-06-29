package dev.combatlab.client.hud;

public record HudOutlineSegment(int start, int end) {
  public HudOutlineSegment {
    if (end < start) {
      throw new IllegalArgumentException("end must not be before start");
    }
  }
}
