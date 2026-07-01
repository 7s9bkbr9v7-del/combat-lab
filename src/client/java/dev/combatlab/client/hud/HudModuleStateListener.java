package dev.combatlab.client.hud;

public interface HudModuleStateListener {
  HudModuleStateListener NONE = HudModuleStateListener::ignoreStateChange;

  void onHudModuleStateChanged(String id, boolean enabled);

  @SuppressWarnings("unused")
  private static void ignoreStateChange(String id, boolean enabled) {}
}
