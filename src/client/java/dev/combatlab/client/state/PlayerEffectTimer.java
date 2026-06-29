package dev.combatlab.client.state;

import java.util.Objects;
import net.minecraft.resources.Identifier;

public record PlayerEffectTimer(
    String id,
    String displayName,
    int amplifier,
    int durationTicks,
    boolean infinite,
    boolean ambient,
    int color,
    Identifier iconTexture) {
  public PlayerEffectTimer {
    Objects.requireNonNull(id, "id");
    Objects.requireNonNull(displayName, "displayName");
  }
}
