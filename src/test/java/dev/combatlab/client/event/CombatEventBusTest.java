package dev.combatlab.client.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class CombatEventBusTest {
  @Test
  void publishesEventsToSubscribersInRegistrationOrder() {
    CombatEventBus bus = new CombatEventBus();
    List<String> received = new ArrayList<>();

    bus.subscribe(CombatClickEvent.class, event -> received.add("first:" + event.clickCount()));
    bus.subscribe(CombatClickEvent.class, event -> received.add("second:" + event.clickCount()));

    bus.publish(new CombatClickEvent(2, 100L));

    assertEquals(List.of("first:2", "second:2"), received);
  }

  @Test
  void subscriptionCanBeClosed() {
    CombatEventBus bus = new CombatEventBus();
    List<Integer> received = new ArrayList<>();
    CombatEventBus.Subscription subscription =
        bus.subscribe(CombatClickEvent.class, event -> received.add(event.clickCount()));

    subscription.close();
    bus.publish(new CombatClickEvent(1, 100L));

    assertEquals(List.of(), received);
  }

  @Test
  void clickEventsRejectNonPositiveCounts() {
    assertThrows(IllegalArgumentException.class, () -> new CombatClickEvent(0, 100L));
  }
}
