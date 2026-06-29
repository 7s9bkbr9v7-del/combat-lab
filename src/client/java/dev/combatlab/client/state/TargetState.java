package dev.combatlab.client.state;

import java.util.UUID;

public record TargetState(UUID id, String name, float distance) {
  public static TargetState none() {
    return new TargetState(null, null, 0.0F);
  }

  public boolean present() {
    return id != null;
  }
}
