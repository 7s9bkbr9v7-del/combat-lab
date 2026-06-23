package dev.combatlab.client.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public final class CombatEventBus {
	private final Map<Class<? extends CombatEvent>, List<Consumer<CombatEvent>>> subscribers = new HashMap<>();

	public <T extends CombatEvent> Subscription subscribe(Class<T> eventType, Consumer<? super T> subscriber) {
		List<Consumer<CombatEvent>> eventSubscribers = subscribers.computeIfAbsent(eventType, ignored -> new ArrayList<>());
		Consumer<CombatEvent> wrapper = event -> subscriber.accept(eventType.cast(event));
		eventSubscribers.add(wrapper);
		return () -> eventSubscribers.remove(wrapper);
	}

	public void publish(CombatEvent event) {
		List<Consumer<CombatEvent>> eventSubscribers = subscribers.get(event.getClass());
		if (eventSubscribers == null || eventSubscribers.isEmpty()) {
			return;
		}

		for (Consumer<CombatEvent> subscriber : List.copyOf(eventSubscribers)) {
			subscriber.accept(event);
		}
	}

	@FunctionalInterface
	public interface Subscription extends AutoCloseable {
		@Override
		void close();
	}
}
