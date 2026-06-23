package dev.combatlab.client.hud;

import java.util.List;

public record HudSnapResult(HudPosition position, List<HudSnapGuide> guides) {
	public HudSnapResult {
		guides = List.copyOf(guides);
	}
}
