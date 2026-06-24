package dev.combatlab.client.hud;

/**
 * Describes whether a normalized HUD module position is snapped to the screen
 * perimeter. Adaptive modules can use this to choose compact edge layouts
 * without duplicating floating-point edge checks.
 */
public record HudEdgeContact(boolean sideEdge, boolean topOrBottomEdge) {
	private static final double EDGE_EPSILON = 1.0E-6;

	public static HudEdgeContact fromNormalizedPosition(double normalizedX, double normalizedY) {
		return new HudEdgeContact(isEdge(normalizedX), isEdge(normalizedY));
	}

	public boolean corner() {
		return sideEdge && topOrBottomEdge;
	}

	private static boolean isEdge(double normalizedPosition) {
		return normalizedPosition <= EDGE_EPSILON || normalizedPosition >= 1.0 - EDGE_EPSILON;
	}
}
