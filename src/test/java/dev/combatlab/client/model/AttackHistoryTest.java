package dev.combatlab.client.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AttackHistoryTest {
	@Test
	void assignsSequencesAndKeepsOnlyTheConfiguredCapacity() {
		AttackHistory history = new AttackHistory(2);
		AttackEvent first = history.record(sample(10));
		AttackEvent second = history.record(sample(11));
		AttackEvent third = history.record(sample(12));

		assertEquals(1, first.sequence());
		assertEquals(2, second.sequence());
		assertEquals(3, third.sequence());
		assertEquals(3, history.totalRecorded());
		assertEquals(List.of(second, third), history.snapshot());
	}

	@Test
	void rejectsNonPositiveCapacity() {
		assertThrows(IllegalArgumentException.class, () -> new AttackHistory(0));
	}

	private static AttackSample sample(long tick) {
		return new AttackSample(tick, tick * 100, null, null, 0.0F, 1.0F, 20);
	}
}
