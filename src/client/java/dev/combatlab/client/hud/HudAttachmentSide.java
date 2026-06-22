package dev.combatlab.client.hud;

public enum HudAttachmentSide {
	LEFT_OF,
	RIGHT_OF,
	ABOVE,
	BELOW;

	public HudPosition resolve(HudRectangle target, HudSize attachedSize, int offset) {
		return switch (this) {
			case LEFT_OF -> new HudPosition(target.x() - attachedSize.width(), target.y() + offset);
			case RIGHT_OF -> new HudPosition(target.right(), target.y() + offset);
			case ABOVE -> new HudPosition(target.x() + offset, target.y() - attachedSize.height());
			case BELOW -> new HudPosition(target.x() + offset, target.bottom());
		};
	}

	public static HudAttachmentSide fromStored(String stored) {
		if (stored == null) {
			return null;
		}
		try {
			return valueOf(stored);
		} catch (IllegalArgumentException ignored) {
			return null;
		}
	}
}
