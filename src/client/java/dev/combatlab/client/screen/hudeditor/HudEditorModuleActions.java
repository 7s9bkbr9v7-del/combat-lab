package dev.combatlab.client.screen.hudeditor;

import dev.combatlab.client.hud.HudModule;
import dev.combatlab.client.hud.HudModuleRegistry;
import dev.combatlab.client.hud.HudPosition;
import dev.combatlab.client.hud.HudRectangle;
import dev.combatlab.client.hud.HudSize;
import dev.combatlab.client.hud.HudSnapper;
import java.util.List;

public final class HudEditorModuleActions {
  private final HudModuleRegistry modules;
  private final HudSelection selection;
  private final HudEditorHistory history;
  private final int addSnapThreshold;

  public HudEditorModuleActions(
      HudModuleRegistry modules,
      HudSelection selection,
      HudEditorHistory history,
      int addSnapThreshold) {
    this.modules = modules;
    this.selection = selection;
    this.history = history;
    this.addSnapThreshold = addSnapThreshold;
  }

  public void disable(HudModule module) {
    history.recordChange(() -> modules.setEnabled(module.id().toString(), false));
  }

  public void disableAll(List<HudModule> selectedModules) {
    history.recordChange(
        () -> {
          for (HudModule module : selectedModules) {
            modules.setEnabled(module.id().toString(), false);
          }
        });
  }

  public void enableAt(
      String id, int requestedX, int requestedY, int screenWidth, int screenHeight) {
    history.beginChange();
    modules.setEnabled(id, true);
    HudModule added = modules.module(id);
    if (added == null) {
      history.commitChange();
      return;
    }

    HudSize size = added.size();
    int x = Math.clamp(requestedX, 0, Math.max(0, screenWidth - size.width()));
    int y = Math.clamp(requestedY, 0, Math.max(0, screenHeight - size.height()));
    List<HudSelection.ModuleRectangle> others =
        selection.enabledModuleRectanglesExcept(added, screenWidth, screenHeight);
    HudPosition snapped =
        HudSnapper.snapToEdges(
            new HudRectangle(x, y, size.width(), size.height()),
            others.stream().map(HudSelection.ModuleRectangle::rectangle).toList(),
            addSnapThreshold,
            screenWidth,
            screenHeight);
    HudRectangle snappedRectangle =
        new HudRectangle(snapped.x(), snapped.y(), size.width(), size.height());
    HudModuleAttachment.placeAndAttach(
        selection, added, snappedRectangle, others, addSnapThreshold, screenWidth, screenHeight);
    added.savePosition();
    history.commitChange();
  }
}
