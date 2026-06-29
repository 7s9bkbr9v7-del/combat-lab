package dev.combatlab.client.hud;

public final class PingText {
  private PingText() {}

  public static String resolve(int ping) {
    return ping < 0 ? "-- ms" : ping + " ms";
  }
}
