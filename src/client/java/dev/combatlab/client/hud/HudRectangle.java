package dev.combatlab.client.hud;

public record HudRectangle(int x, int y, int width, int height) {
	public int right() {
		return x + width;
	}

	public int bottom() {
		return y + height;
	}
}
