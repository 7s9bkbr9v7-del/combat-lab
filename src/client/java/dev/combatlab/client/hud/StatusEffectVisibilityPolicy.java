package dev.combatlab.client.hud;

@FunctionalInterface
public interface StatusEffectVisibilityPolicy {
  StatusEffectVisibilityPolicy SHOW_ALL = ignored -> false;

  boolean shouldHide(String effectId);
}
