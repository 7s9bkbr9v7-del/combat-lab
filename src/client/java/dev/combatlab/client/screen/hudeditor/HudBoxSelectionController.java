package dev.combatlab.client.screen.hudeditor;

import dev.combatlab.client.hud.HudRectangle;

public final class HudBoxSelectionController {
  private final HudSelection selection;
  private final HudModuleSelection moduleSelection;
  private boolean active;
  private boolean additive;
  private boolean contextSelection;
  private int startX;
  private int startY;
  private int currentX;
  private int currentY;

  public HudBoxSelectionController(HudSelection selection, HudModuleSelection moduleSelection) {
    this.selection = selection;
    this.moduleSelection = moduleSelection;
  }

  public void begin(double mouseX, double mouseY, boolean additive) {
    begin(mouseX, mouseY, additive, false);
  }

  public void beginForContextMenu(double mouseX, double mouseY) {
    begin(mouseX, mouseY, false, true);
  }

  private void begin(double mouseX, double mouseY, boolean additive, boolean contextSelection) {
    this.active = true;
    this.additive = additive;
    this.contextSelection = contextSelection;
    this.startX = (int) mouseX;
    this.startY = (int) mouseY;
    this.currentX = startX;
    this.currentY = startY;
    if (!additive) {
      moduleSelection.clear();
    }
  }

  public boolean drag(double mouseX, double mouseY, int screenWidth, int screenHeight) {
    if (!active) {
      return false;
    }
    currentX = (int) mouseX;
    currentY = (int) mouseY;
    moduleSelection.selectWithin(
        bounds(),
        selection.enabledModuleRectanglesExcept(null, screenWidth, screenHeight),
        additive);
    return true;
  }

  public boolean release() {
    if (!active) {
      return false;
    }
    active = false;
    return true;
  }

  public boolean contextSelection() {
    return contextSelection;
  }

  public boolean active() {
    return active;
  }

  public HudRectangle bounds() {
    int x = Math.min(startX, currentX);
    int y = Math.min(startY, currentY);
    return new HudRectangle(x, y, Math.abs(currentX - startX), Math.abs(currentY - startY));
  }
}
