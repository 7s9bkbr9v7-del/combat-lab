package dev.combatlab.client.hud;

public final class HudOrientationResolver {
	private HudOrientationResolver() {
	}

	public static HudOrientation resolve(HudRectangle module, int screenWidth, int screenHeight) {
		int centerX = screenWidth / 2;
		int centerY = screenHeight / 2;
		HudHorizontalSide horizontalSide = module.centerX() <= centerX
				? HudHorizontalSide.RIGHT
				: HudHorizontalSide.LEFT;
		HudVerticalSide verticalSide = module.centerY() <= centerY
				? HudVerticalSide.BOTTOM
				: HudVerticalSide.TOP;

		return new HudOrientation(horizontalSide, verticalSide, cornerFacing(module, centerX, centerY));
	}

	public static HudCorner cornerFacing(HudRectangle module, int targetX, int targetY) {
		HudCorner nearest = HudCorner.TOP_LEFT;
		long nearestDistance = Long.MAX_VALUE;
		for (HudCorner corner : HudCorner.values()) {
			long distance = corner.distanceSquared(module, targetX, targetY);
			if (distance < nearestDistance) {
				nearest = corner;
				nearestDistance = distance;
			}
		}
		return nearest;
	}
}
