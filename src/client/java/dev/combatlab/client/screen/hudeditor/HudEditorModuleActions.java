package dev.combatlab.client.screen.hudeditor;

import dev.combatlab.client.hud.HudModule;
import dev.combatlab.client.hud.HudModuleRegistry;
import dev.combatlab.client.hud.HudPosition;
import dev.combatlab.client.hud.HudRectangle;
import dev.combatlab.client.hud.HudSize;
import dev.combatlab.client.hud.HudSnapper;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

  public void disable(HudModule module, int screenWidth, int screenHeight) {
    history.recordChange(
        () -> modules.setEnabled(module.id().toString(), false, screenWidth, screenHeight));
  }

  public void disableAll(List<HudModule> selectedModules, int screenWidth, int screenHeight) {
    history.recordChange(
        () -> {
          Set<String> disabledIds =
              selectedModules.stream()
                  .map(module -> module.id().toString())
                  .collect(Collectors.toSet());
          for (HudModule module : selectedModules) {
            promoteFirstChild(module, disabledIds, screenWidth, screenHeight);
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

  private void promoteFirstChild(
      HudModule disabledModule, Set<String> disabledIds, int screenWidth, int screenHeight) {
    HudModule child = firstEnabledChild(disabledModule, disabledIds);
    if (child == null) {
      return;
    }

    HudRectangle disabledBounds = selection.rectangle(disabledModule, screenWidth, screenHeight);
    child.clearAttachment();
    child.updatePosition(disabledBounds.x(), disabledBounds.y(), screenWidth, screenHeight);
    child.savePosition();
  }

  private HudModule firstEnabledChild(HudModule parent, Set<String> disabledIds) {
    String parentId = parent.id().toString();
    for (HudModule candidate : modules.modules()) {
      if (candidate.enabled()
          && !disabledIds.contains(candidate.id().toString())
          && parentId.equals(candidate.attachmentTargetId())) {
        return candidate;
      }
    }
    return null;
  }
}
