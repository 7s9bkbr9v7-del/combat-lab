package dev.combatlab.client.state;

import java.util.List;
import net.minecraft.resources.Identifier;

public record PlayerEffects(List<PlayerEffectTimer> active) {
  private static final PlayerEffects EMPTY = new PlayerEffects(List.of());
  private static final PlayerEffects EDITOR_PREVIEW =
      new PlayerEffects(
          List.of(
              new PlayerEffectTimer(
                  "minecraft:speed",
                  "Speed",
                  1,
                  3 * 60 * 20 + 24 * 20,
                  false,
                  false,
                  0x7CAFC6,
                  Identifier.fromNamespaceAndPath("minecraft", "textures/mob_effect/speed.png")),
              new PlayerEffectTimer(
                  "minecraft:strength",
                  "Strength",
                  0,
                  58 * 20,
                  false,
                  false,
                  0x932423,
                  Identifier.fromNamespaceAndPath(
                      "minecraft", "textures/mob_effect/strength.png"))));

  public PlayerEffects {
    active = List.copyOf(active);
  }

  public static PlayerEffects empty() {
    return EMPTY;
  }

  public static PlayerEffects editorPreview(PlayerEffects effects) {
    return effects.active().isEmpty() ? EDITOR_PREVIEW : effects;
  }

  public boolean emptyActiveEffects() {
    return active.isEmpty();
  }
}
