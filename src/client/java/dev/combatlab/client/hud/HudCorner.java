package dev.combatlab.client.hud;

public enum HudCorner {
	TOP_LEFT(HudHorizontalSide.LEFT, HudVerticalSide.TOP) {
		@Override
		public int x(HudRectangle rectangle) {
			return rectangle.x();
		}

		@Override
		public int y(HudRectangle rectangle) {
			return rectangle.y();
		}

		@Override
		public int oppositeX(HudRectangle rectangle) {
			return rectangle.right();
		}

		@Override
		public int oppositeY(HudRectangle rectangle) {
			return rectangle.bottom();
		}

		@Override
		public double widthFromMouse(int oppositeX, double mouseX) {
			return oppositeX - mouseX;
		}

		@Override
		public double heightFromMouse(int oppositeY, double mouseY) {
			return oppositeY - mouseY;
		}

		@Override
		public int resizedX(int oppositeX, HudSize size) {
			return oppositeX - size.width();
		}

		@Override
		public int resizedY(int oppositeY, HudSize size) {
			return oppositeY - size.height();
		}

		@Override
		public double maxScale(int oppositeX, int oppositeY, int screenWidth, int screenHeight, HudSize unscaled) {
			return Math.min((double) oppositeX / unscaled.width(), (double) oppositeY / unscaled.height());
		}
	},
	TOP_RIGHT(HudHorizontalSide.RIGHT, HudVerticalSide.TOP) {
		@Override
		public int x(HudRectangle rectangle) {
			return rectangle.right();
		}

		@Override
		public int y(HudRectangle rectangle) {
			return rectangle.y();
		}

		@Override
		public int oppositeX(HudRectangle rectangle) {
			return rectangle.x();
		}

		@Override
		public int oppositeY(HudRectangle rectangle) {
			return rectangle.bottom();
		}

		@Override
		public double widthFromMouse(int oppositeX, double mouseX) {
			return mouseX - oppositeX;
		}

		@Override
		public double heightFromMouse(int oppositeY, double mouseY) {
			return oppositeY - mouseY;
		}

		@Override
		public int resizedX(int oppositeX, HudSize size) {
			return oppositeX;
		}

		@Override
		public int resizedY(int oppositeY, HudSize size) {
			return oppositeY - size.height();
		}

		@Override
		public double maxScale(int oppositeX, int oppositeY, int screenWidth, int screenHeight, HudSize unscaled) {
			return Math.min((double) (screenWidth - oppositeX) / unscaled.width(), (double) oppositeY / unscaled.height());
		}
	},
	BOTTOM_RIGHT(HudHorizontalSide.RIGHT, HudVerticalSide.BOTTOM) {
		@Override
		public int x(HudRectangle rectangle) {
			return rectangle.right();
		}

		@Override
		public int y(HudRectangle rectangle) {
			return rectangle.bottom();
		}

		@Override
		public int oppositeX(HudRectangle rectangle) {
			return rectangle.x();
		}

		@Override
		public int oppositeY(HudRectangle rectangle) {
			return rectangle.y();
		}

		@Override
		public double widthFromMouse(int oppositeX, double mouseX) {
			return mouseX - oppositeX;
		}

		@Override
		public double heightFromMouse(int oppositeY, double mouseY) {
			return mouseY - oppositeY;
		}

		@Override
		public int resizedX(int oppositeX, HudSize size) {
			return oppositeX;
		}

		@Override
		public int resizedY(int oppositeY, HudSize size) {
			return oppositeY;
		}

		@Override
		public double maxScale(int oppositeX, int oppositeY, int screenWidth, int screenHeight, HudSize unscaled) {
			return Math.min((double) (screenWidth - oppositeX) / unscaled.width(), (double) (screenHeight - oppositeY) / unscaled.height());
		}
	},
	BOTTOM_LEFT(HudHorizontalSide.LEFT, HudVerticalSide.BOTTOM) {
		@Override
		public int x(HudRectangle rectangle) {
			return rectangle.x();
		}

		@Override
		public int y(HudRectangle rectangle) {
			return rectangle.bottom();
		}

		@Override
		public int oppositeX(HudRectangle rectangle) {
			return rectangle.right();
		}

		@Override
		public int oppositeY(HudRectangle rectangle) {
			return rectangle.y();
		}

		@Override
		public double widthFromMouse(int oppositeX, double mouseX) {
			return oppositeX - mouseX;
		}

		@Override
		public double heightFromMouse(int oppositeY, double mouseY) {
			return mouseY - oppositeY;
		}

		@Override
		public int resizedX(int oppositeX, HudSize size) {
			return oppositeX - size.width();
		}

		@Override
		public int resizedY(int oppositeY, HudSize size) {
			return oppositeY;
		}

		@Override
		public double maxScale(int oppositeX, int oppositeY, int screenWidth, int screenHeight, HudSize unscaled) {
			return Math.min((double) oppositeX / unscaled.width(), (double) (screenHeight - oppositeY) / unscaled.height());
		}
	};

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

	public long distanceSquared(HudRectangle rectangle, int targetX, int targetY) {
		long deltaX = x(rectangle) - targetX;
		long deltaY = y(rectangle) - targetY;
		return deltaX * deltaX + deltaY * deltaY;
	}

	public abstract int x(HudRectangle rectangle);

	public abstract int y(HudRectangle rectangle);

	public abstract int oppositeX(HudRectangle rectangle);

	public abstract int oppositeY(HudRectangle rectangle);

	public abstract double widthFromMouse(int oppositeX, double mouseX);

	public abstract double heightFromMouse(int oppositeY, double mouseY);

	public abstract int resizedX(int oppositeX, HudSize size);

	public abstract int resizedY(int oppositeY, HudSize size);

	public abstract double maxScale(int oppositeX, int oppositeY, int screenWidth, int screenHeight, HudSize unscaled);
}
