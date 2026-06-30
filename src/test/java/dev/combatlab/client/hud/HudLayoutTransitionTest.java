package dev.combatlab.client.hud;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class HudLayoutTransitionTest {
  @Test
  void rejectsNonPositiveDurations() {
    assertThrows(IllegalArgumentException.class, () -> new HudLayoutTransition<>(0L));
    assertThrows(IllegalArgumentException.class, () -> new HudLayoutTransition<>(-1L));
  }
}
