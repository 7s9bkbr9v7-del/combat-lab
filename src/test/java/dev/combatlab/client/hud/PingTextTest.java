package dev.combatlab.client.hud;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PingTextTest {
	@Test
	void formatsKnownPingInMilliseconds() {
		assertEquals("42 ms", PingText.resolve(42));
	}

	@Test
	void showsPlaceholderWhenPingIsUnavailable() {
		assertEquals("-- ms", PingText.resolve(-1));
	}
}
