package dev.combatlab.client.hud;

/**
 * A HUD module whose dimensions can change in response to its screen position. The editor locks
 * that layout during pointer interactions to prevent size and position changes from feeding back
 * into one another.
 */
public interface AdaptiveLayoutHudModule extends HudModule {
  void lockLayout();

  void unlockLayout();
}
