package dev.combatlab.client.model;

import java.util.ArrayDeque;
import java.util.List;

/**
 * A bounded timeline. Keeping this small makes recording safe during long sessions.
 */
public final class AttackHistory {
	private final ArrayDeque<AttackEvent> events;
	private final int capacity;
	private long sequence;

	public AttackHistory(int capacity) {
		if (capacity < 1) {
			throw new IllegalArgumentException("capacity must be positive");
		}
		this.capacity = capacity;
		this.events = new ArrayDeque<>(capacity);
	}

	public AttackEvent record(AttackSample sample) {
		AttackEvent event = new AttackEvent(
				++sequence,
				sample.gameTick(),
				sample.capturedAtNanos(),
				sample.targetId(),
				sample.targetName(),
				sample.targetDistance(),
				sample.attackStrength(),
				sample.ping()
		);
		if (events.size() == capacity) {
			events.removeFirst();
		}
		events.addLast(event);
		return event;
	}

	public AttackEvent latest() {
		return events.peekLast();
	}

	public int size() {
		return events.size();
	}

	public long totalRecorded() {
		return sequence;
	}

	public List<AttackEvent> snapshot() {
		return List.copyOf(events);
	}
}
