package dev.combatlab.client.state;

public record DirectionState(boolean present, int bearingDegrees) {
  public static DirectionState absent() {
    return new DirectionState(false, 0);
  }

  public static DirectionState of(float bearingDegrees) {
    return new DirectionState(true, normalize(Math.round(bearingDegrees)));
  }

  private static int normalize(int degrees) {
    int normalized = degrees % 360;
    return normalized < 0 ? normalized + 360 : normalized;
  }
}
