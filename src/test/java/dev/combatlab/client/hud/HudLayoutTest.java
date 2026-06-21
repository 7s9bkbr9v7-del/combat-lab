package dev.combatlab.client.hud;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HudLayoutTest {
	@Test
	void resolvesAndNormalizesWithinAvailableTravel() {
		HudSize size = new HudSize(200, 100);
		HudPosition position = HudLayout.resolve(0.5, 0.25, 1000, 500, size);

		assertEquals(new HudPosition(400, 100), position);
		assertEquals(0.5, HudLayout.normalizeX(position.x(), 1000, size), 0.0001);
		assertEquals(0.25, HudLayout.normalizeY(position.y(), 500, size), 0.0001);
	}

	@Test
	void clampsCoordinatesAndHandlesOversizedModules() {
		assertEquals(new HudPosition(0, 0), HudLayout.resolve(2.0, -1.0, 100, 50, new HudSize(200, 100)));
	}
}
