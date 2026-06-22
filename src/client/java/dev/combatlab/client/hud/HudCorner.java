package dev.combatlab.client.hud;

public enum HudCorner {
	TOP_LEFT(HudHorizontalSide.LEFT, HudVerticalSide.TOP),
	TOP_RIGHT(HudHorizontalSide.RIGHT, HudVerticalSide.TOP),
	BOTTOM_RIGHT(HudHorizontalSide.RIGHT, HudVerticalSide.BOTTOM),
	BOTTOM_LEFT(HudHorizontalSide.LEFT, HudVerticalSide.BOTTOM);

	private final HudHorizontalSide horizontalSide;
	private final HudVerticalSide verticalSide;

	HudCorner(HudHorizontalSide horizontalSide, HudVerticalSide verticalSide) {
		this.horizontalSide = horizontalSide;
		this.verticalSide = verticalSide;
	}

	public HudHorizontalSide horizontalSide() {
		return horizontalSide;
	}

	public HudVerticalSide verticalSide() {
		return verticalSide;
	}

	public int x(HudRectangle rectangle) {
		return facesLeft() ? rectangle.x() : rectangle.right();
	}

	public int y(HudRectangle rectangle) {
		return facesTop() ? rectangle.y() : rectangle.bottom();
	}

	public int oppositeX(HudRectangle rectangle) {
		return facesLeft() ? rectangle.right() : rectangle.x();
	}

	public int oppositeY(HudRectangle rectangle) {
		return facesTop() ? rectangle.bottom() : rectangle.y();
	}

	public double widthFromMouse(int oppositeX, double mouseX) {
		return facesLeft() ? oppositeX - mouseX : mouseX - oppositeX;
	}

	public double heightFromMouse(int oppositeY, double mouseY) {
		return facesTop() ? oppositeY - mouseY : mouseY - oppositeY;
	}

	public int resizedX(int oppositeX, HudSize size) {
		return facesLeft() ? oppositeX - size.width() : oppositeX;
	}

	public int resizedY(int oppositeY, HudSize size) {
		return facesTop() ? oppositeY - size.height() : oppositeY;
	}

	public double maxScale(int oppositeX, int oppositeY, int screenWidth, int screenHeight, HudSize unscaled) {
		double availableWidth = facesLeft() ? oppositeX : screenWidth - oppositeX;
		double availableHeight = facesTop() ? oppositeY : screenHeight - oppositeY;
		return Math.min(availableWidth / unscaled.width(), availableHeight / unscaled.height());
	}

	public long distanceSquared(HudRectangle rectangle, int targetX, int targetY) {
		long deltaX = x(rectangle) - targetX;
		long deltaY = y(rectangle) - targetY;
		return deltaX * deltaX + deltaY * deltaY;
	}

	private boolean facesLeft() {
		return horizontalSide == HudHorizontalSide.LEFT;
	}

	private boolean facesTop() {
		return verticalSide == HudVerticalSide.TOP;
	}
}
