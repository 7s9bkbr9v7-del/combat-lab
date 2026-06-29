package dev.combatlab.client.hud;

public record HudSnapGuide(Axis axis, int coordinate) {
  public enum Axis {
    VERTICAL,
    HORIZONTAL
  }
}
