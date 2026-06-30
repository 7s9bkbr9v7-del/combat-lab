package dev.combatlab.client.hud;

import java.util.List;

/**
 * A HUD module whose dimensions can change in response to its screen position. The editor locks
 * that layout during pointer interactions to prevent size and position changes from feeding back
 * into one another.
 */
public interface AdaptiveLayoutHudModule extends HudModule {
  String ADAPTIVE_LAYOUT = "ADAPTIVE";

  List<String> availableLayouts();

  String currentLayout();

  void cycleLayout();

  void lockLayout();

  void unlockLayout();
}
