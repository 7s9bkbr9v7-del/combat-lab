package dev.combatlab.client.hud;

public enum HudAttachmentSide {
  LEFT_OF,
  RIGHT_OF,
  ABOVE,
  BELOW;

  public HudPosition resolve(HudRectangle target, HudSize attachedSize, int offset) {
    int clampedOffset = clampedOffset(target, attachedSize, offset);
    return new HudPosition(
        x(target, attachedSize, clampedOffset), y(target, attachedSize, clampedOffset));
  }

  void resolveInto(
      HudRectangle target, HudSize attachedSize, int offset, HudRectangle attachedBounds) {
    int clampedOffset = clampedOffset(target, attachedSize, offset);
    attachedBounds.set(
        x(target, attachedSize, clampedOffset),
        y(target, attachedSize, clampedOffset),
        attachedSize.width(),
        attachedSize.height());
  }

  private int x(HudRectangle target, HudSize attachedSize, int clampedOffset) {
    return switch (this) {
      case LEFT_OF -> target.x() - attachedSize.width();
      case RIGHT_OF -> target.right();
      case ABOVE, BELOW -> target.x() + clampedOffset;
    };
  }

  private int y(HudRectangle target, HudSize attachedSize, int clampedOffset) {
    return switch (this) {
      case LEFT_OF, RIGHT_OF -> target.y() + clampedOffset;
      case ABOVE -> target.y() - attachedSize.height();
      case BELOW -> target.bottom();
    };
  }

  private int clampedOffset(HudRectangle target, HudSize attachedSize, int offset) {
    return switch (this) {
      case LEFT_OF, RIGHT_OF -> Math.clamp(offset, -attachedSize.height(), target.height());
      case ABOVE, BELOW -> Math.clamp(offset, -attachedSize.width(), target.width());
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
