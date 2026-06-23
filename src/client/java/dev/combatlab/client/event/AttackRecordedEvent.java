package dev.combatlab.client.event;

import dev.combatlab.client.model.AttackEvent;

public record AttackRecordedEvent(AttackEvent attack) implements CombatEvent {
	@Override
	public long capturedAtNanos() {
		return attack.capturedAtNanos();
	}
}
