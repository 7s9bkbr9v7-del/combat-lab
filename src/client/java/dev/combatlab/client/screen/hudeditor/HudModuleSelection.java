package dev.combatlab.client.screen.hudeditor;

import dev.combatlab.client.hud.HudModule;
import dev.combatlab.client.hud.HudRectangle;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class HudModuleSelection {
  private final Set<String> selectedIds = new LinkedHashSet<>();
  private String rangeAnchorId;

  public void select(
      HudModule module, boolean addToSelection, boolean rangeSelect, List<HudModule> modules) {
    String moduleId = module.id().toString();
    if (rangeSelect && rangeAnchorId != null) {
      List<String> range = attachmentPath(rangeAnchorId, moduleId, modules);
      if (!range.isEmpty()) {
        if (!addToSelection) {
          selectedIds.clear();
        }
        selectedIds.addAll(range);
        rangeAnchorId = moduleId;
        return;
      }
    }

    if (addToSelection) {
      if (!selectedIds.remove(moduleId)) {
        selectedIds.add(moduleId);
      }
    } else {
      selectedIds.clear();
      selectedIds.add(moduleId);
    }
    rangeAnchorId = moduleId;
  }

  public void clear() {
    selectedIds.clear();
    rangeAnchorId = null;
  }

  public void selectAll(List<HudModule> modules) {
    selectedIds.clear();
    for (HudModule module : modules) {
      if (module.enabled()) {
        selectedIds.add(module.id().toString());
        rangeAnchorId = module.id().toString();
      }
    }
  }

  public boolean selected(HudModule module) {
    return selectedIds.contains(module.id().toString());
  }

  public int size() {
    return selectedIds.size();
  }

  public boolean hasMultipleSelected() {
    return selectedIds.size() > 1;
  }

  public List<HudModule> selectedModules(List<HudModule> modules) {
    return modules.stream().filter(this::selected).toList();
  }

  public void selectWithin(
      HudRectangle selectionBounds,
      List<HudSelection.ModuleRectangle> modules,
      boolean addToSelection) {
    if (!addToSelection) {
      selectedIds.clear();
    }
    for (HudSelection.ModuleRectangle module : modules) {
      if (selectionBounds.intersects(module.rectangle())) {
        selectedIds.add(module.module().id().toString());
        rangeAnchorId = module.module().id().toString();
      }
    }
  }

  private static List<String> attachmentPath(
      String firstId, String secondId, List<HudModule> modules) {
    List<String> firstAncestors = ancestors(firstId, modules);
    List<String> secondAncestors = ancestors(secondId, modules);
    for (String commonId : firstAncestors) {
      if (secondAncestors.contains(commonId)) {
        List<String> path = new ArrayList<>();
        path.addAll(firstAncestors.subList(0, firstAncestors.indexOf(commonId) + 1));
        List<String> secondToCommon = secondAncestors.subList(0, secondAncestors.indexOf(commonId));
        for (int index = secondToCommon.size() - 1; index >= 0; index--) {
          path.add(secondToCommon.get(index));
        }
        return path;
      }
    }
    return List.of();
  }

  private static List<String> ancestors(String moduleId, List<HudModule> modules) {
    List<String> ancestors = new ArrayList<>();
    String currentId = moduleId;
    for (int depth = 0; depth <= modules.size(); depth++) {
      HudModule current = module(currentId, modules);
      if (current == null) {
        return List.of();
      }
      ancestors.add(currentId);
      String parentId = current.attachmentTargetId();
      if (parentId == null) {
        return ancestors;
      }
      currentId = parentId;
    }
    return List.of();
  }

  private static HudModule module(String moduleId, List<HudModule> modules) {
    for (HudModule module : modules) {
      if (moduleId.equals(module.id().toString())) {
        return module;
      }
    }
    return null;
  }
}
