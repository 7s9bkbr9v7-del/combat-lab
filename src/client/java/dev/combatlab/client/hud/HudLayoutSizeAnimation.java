package dev.combatlab.client.hud;

final class HudLayoutSizeAnimation<T> {
  private final HudLayoutTransition<T> transition = new HudLayoutTransition<>();
  private double startWidth;
  private double startHeight;
  private double width;
  private double height;
  private boolean hasCurrentSize;

  HudSize update(T target, HudSize targetSize) {
    HudLayoutTransition.Update update = transition.update(target);
    if (update.firstUpdate()) {
      set(targetSize);
      return targetSize;
    }
    if (update.targetChanged()) {
      startWidth = width;
      startHeight = height;
    }

    width = HudLayoutTransition.lerp(startWidth, targetSize.width(), update.progress());
    height = HudLayoutTransition.lerp(startHeight, targetSize.height(), update.progress());
    if (update.complete()) {
      set(targetSize);
    }
    return new HudSize(Math.max(1, (int) Math.round(width)), Math.max(1, (int) Math.round(height)));
  }

  HudSize snapTo(T target, HudSize targetSize) {
    transition.snapTo(target);
    set(targetSize);
    return targetSize;
  }

  HudAnimatedSize currentSize(HudSize fallback) {
    if (!hasCurrentSize) {
      return new HudAnimatedSize(fallback.width(), fallback.height());
    }
    return new HudAnimatedSize(width, height);
  }

  void reset() {
    transition.reset();
    hasCurrentSize = false;
  }

  private void set(HudSize size) {
    startWidth = size.width();
    startHeight = size.height();
    width = size.width();
    height = size.height();
    hasCurrentSize = true;
  }
}
