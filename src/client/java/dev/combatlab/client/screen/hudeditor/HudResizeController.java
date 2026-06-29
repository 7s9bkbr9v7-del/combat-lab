package dev.combatlab.client.screen.hudeditor;

import dev.combatlab.client.debug.DebugLogger;
import dev.combatlab.client.hud.AdaptiveLayoutHudModule;
import dev.combatlab.client.hud.HudCorner;
import dev.combatlab.client.hud.HudRectangle;
import dev.combatlab.client.hud.HudSize;
import dev.combatlab.client.hud.ResizableHudModule;

public final class HudResizeController {
  private final HudSelection selection;
  private final DebugLogger debug;
  private final int handleSize;
  private ResizableHudModule resizedModule;
  private HudCorner resizedCorner;

  public HudResizeController(HudSelection selection, DebugLogger debug, int handleSize) {
    this.selection = selection;
    this.debug = debug;
    this.handleSize = handleSize;
  }

  public boolean begin(double mouseX, double mouseY, int screenWidth, int screenHeight) {
    HudSelection.ResizeSelection selected =
        selection.topResizeHandleAt(mouseX, mouseY, screenWidth, screenHeight, handleSize);
    if (selected == null) {
      return false;
    }
    resizedModule = selected.module();
    resizedCorner = selected.corner();
    resizedModule.detach(screenWidth, screenHeight);
    if (resizedModule instanceof AdaptiveLayoutHudModule adaptive) {
      adaptive.lockLayout();
    }
    return true;
  }

  public boolean resize(double mouseX, double mouseY, int screenWidth, int screenHeight) {
    if (resizedModule == null || resizedCorner == null) {
      return false;
    }

    HudRectangle currentRectangle = selection.rectangle(resizedModule, screenWidth, screenHeight);
    HudSize unscaled = resizedModule.unscaledSize();
    int fixedX = resizedCorner.oppositeX(currentRectangle);
    int fixedY = resizedCorner.oppositeY(currentRectangle);
    double requestedScale =
        Math.max(
            resizedCorner.widthFromMouse(fixedX, mouseX) / unscaled.width(),
            resizedCorner.heightFromMouse(fixedY, mouseY) / unscaled.height());
    double maximumScale =
        Math.min(
            resizedModule.maxScale(),
            resizedCorner.maxScale(fixedX, fixedY, screenWidth, screenHeight, unscaled));
    double scale =
        Math.clamp(
            requestedScale,
            resizedModule.minScale(),
            Math.max(resizedModule.minScale(), maximumScale));
    resizedModule.updateScale(scale);
    HudSize scaledSize = resizedModule.size();
    resizedModule.updatePosition(
        resizedCorner.resizedX(fixedX, scaledSize),
        resizedCorner.resizedY(fixedY, scaledSize),
        screenWidth,
        screenHeight);
    return true;
  }

  public boolean release() {
    if (resizedModule == null) {
      return false;
    }
    if (resizedModule instanceof AdaptiveLayoutHudModule adaptive) {
      adaptive.unlockLayout();
    }
    resizedModule.savePosition();
    debug.info(
        "{} resized to {}%",
        resizedModule.displayName().getString(), Math.round(resizedModule.scale() * 100.0));
    resizedModule = null;
    resizedCorner = null;
    return true;
  }

  public ResizableHudModule activeModule() {
    return resizedModule;
  }
}
