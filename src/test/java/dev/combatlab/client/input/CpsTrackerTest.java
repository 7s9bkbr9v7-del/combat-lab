package dev.combatlab.client.input;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class CpsTrackerTest {
  @Test
  void countsClicksInThePreviousOneSecondWindow() {
    CpsTracker tracker = new CpsTracker();
    tracker.recordClicks(2, 100L);
    tracker.recordClicks(1, 500_000_000L);

    assertEquals(3, tracker.currentCps(999_999_999L));
    assertEquals(1, tracker.currentCps(1_000_000_100L));
    assertEquals(0, tracker.currentCps(1_500_000_000L));
  }

  @Test
  void recordsMultipleClicksReportedInOneCallback() {
    CpsTracker tracker = new CpsTracker();
    tracker.recordClicks(4, 1_000L);
    assertEquals(4, tracker.currentCps(2_000L));
  }
}
