package dev.combatlab.client.hud;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HudSnapperTest {
	@Test
	void snapsAdjacentAndAlignedEdges() {
		HudRectangle stationary = new HudRectangle(100, 100, 80, 20);
		HudPosition snapped = HudSnapper.snap(
				new HudRectangle(183, 103, 60, 20),
				List.of(stationary),
				6
		);

		assertEquals(new HudPosition(180, 100), snapped);
	}

	@Test
	void ignoresElementsOutsideThePerpendicularRange() {
		HudPosition snapped = HudSnapper.snap(
				new HudRectangle(130, 300, 60, 20),
				List.of(new HudRectangle(100, 100, 80, 20)),
				6
		);

		assertEquals(new HudPosition(130, 300), snapped);
	}

	@Test
	void alignsCentersAcrossTheScreen() {
		HudPosition snapped = HudSnapper.snap(
				new HudRectangle(109, 300, 60, 20),
				List.of(new HudRectangle(100, 100, 80, 20)),
				6
		);

		assertEquals(new HudPosition(110, 300), snapped);
	}

	@Test
	void snapsToScreenEdgesAndCorners() {
		HudPosition left = HudSnapper.snap(
				new HudRectangle(5, 40, 60, 20),
				List.of(),
				6,
				300,
				200
		);
		assertEquals(new HudPosition(0, 40), left);

		HudPosition bottomRight = HudSnapper.snap(
				new HudRectangle(236, 177, 60, 20),
				List.of(),
				6,
				300,
				200
		);
		assertEquals(new HudPosition(240, 180), bottomRight);
	}

	@Test
	void snapsToHorizontalAndVerticalScreenCentersIndependently() {
		HudPosition horizontalCenter = HudSnapper.snap(
				new HudRectangle(124, 30, 60, 20),
				List.of(),
				6,
				300,
				200
		);
		assertEquals(new HudPosition(120, 30), horizontalCenter);

		HudPosition verticalCenter = HudSnapper.snap(
				new HudRectangle(30, 86, 60, 20),
				List.of(),
				6,
				300,
				200
		);
		assertEquals(new HudPosition(30, 90), verticalCenter);
	}
}
