package dev.combatlab.client.hud;

final class HudAdaptiveLayoutAnimation<T> {
  private final HudLayoutSizeAnimation<T> sizeAnimation = new HudLayoutSizeAnimation<>();

  HudRectangle editorBounds(
      ResizableBaseHudModule module,
      T target,
      HudSize targetSize,
      boolean locked,
      int screenWidth,
      int screenHeight) {
    HudRectangle bounds = new HudRectangle(0, 0, 0, 0);
    module.resolveEditorBoundsInto(
        bounds,
        scaled(previewSize(target, targetSize, locked), module.scale()),
        screenWidth,
        screenHeight);
    return bounds;
  }

  HudSize previewSize(T target, HudSize targetSize, boolean locked) {
    return locked
        ? sizeAnimation.snapTo(target, targetSize)
        : sizeAnimation.update(target, targetSize);
  }

  HudAnimatedSize currentSize(HudSize fallback) {
    return sizeAnimation.currentSize(fallback);
  }

  HudAnimatedSize currentContextSize(HudRenderContext context, double scale) {
    return currentSize(unscaledContextSize(context, scale));
  }

  double centeredRenderX(
      HudRenderContext context,
      double scale,
      double normalizedX,
      boolean attached,
      double animatedWidth,
      int renderedCenterX) {
    if (attached) {
      return context.bounds().x();
    }

    double scaledAnimatedWidth = animatedWidth * scale;
    return resolveAxis(normalizedX, context.screenWidth(), scaledAnimatedWidth)
        + scaledAnimatedWidth / 2.0D
        - renderedCenterX * scale;
  }

  void reset() {
    sizeAnimation.reset();
  }

  private static HudSize scaled(HudSize size, double scale) {
    return new HudSize(
        (int) Math.ceil(size.width() * scale), (int) Math.ceil(size.height() * scale));
  }

  private static HudSize unscaledContextSize(HudRenderContext context, double scale) {
    return new HudSize(
        Math.max(1, (int) Math.round(context.bounds().width() / scale)),
        Math.max(1, (int) Math.round(context.bounds().height() / scale)));
  }

  private static double resolveAxis(double normalized, int screenSize, double size) {
    return Math.clamp(normalized, 0.0D, 1.0D) * Math.max(0.0D, screenSize - size);
  }
}
