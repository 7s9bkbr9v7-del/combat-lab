package dev.combatlab.client.hud;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class MovementStatusTextTest {
  @Test
  void crouchingTakesPriority() {
    assertEquals("Crouched", MovementStatusText.resolve(true, true, true));
  }

  @Test
  void distinguishesHoldAndToggleSprintStates() {
    assertEquals("Sprinting", MovementStatusText.resolve(false, true, false));
    assertEquals("Sprinting (Toggled)", MovementStatusText.resolve(false, true, true));
  }

  @Test
  void showsToggledSprintWheneverToggleSprintIsActive() {
    assertEquals("Sprinting (Toggled)", MovementStatusText.resolve(false, false, true));
  }

  @Test
  void returnsNoVisibleStatusWhenNeitherMovementStateIsActive() {
    assertEquals("", MovementStatusText.resolve(false, false, false));
  }
}
