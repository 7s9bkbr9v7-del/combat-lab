package dev.combatlab.client.screen.hudeditor;

import dev.combatlab.client.config.HudModuleSettings;
import dev.combatlab.client.hud.HudModuleDescriptor;
import dev.combatlab.client.hud.HudModuleRegistry;
import java.util.ArrayDeque;
import java.util.LinkedHashMap;
import java.util.Map;

public final class HudEditorHistory {
  private final HudModuleRegistry modules;
  private final ArrayDeque<Snapshot> undoStack = new ArrayDeque<>();
  private final ArrayDeque<Snapshot> redoStack = new ArrayDeque<>();
  private Snapshot pendingSnapshot;

  public HudEditorHistory(HudModuleRegistry modules) {
    this.modules = modules;
  }

  public void beginChange() {
    pendingSnapshot = capture();
  }

  public void commitChange() {
    if (pendingSnapshot == null) {
      return;
    }
    Snapshot before = pendingSnapshot;
    pendingSnapshot = null;
    if (!before.equals(capture())) {
      undoStack.push(before);
      redoStack.clear();
    }
  }

  public void cancelChange() {
    pendingSnapshot = null;
  }

  public void recordChange(Runnable change) {
    beginChange();
    change.run();
    commitChange();
  }

  public boolean undo() {
    if (undoStack.isEmpty()) {
      return false;
    }
    pendingSnapshot = null;
    redoStack.push(capture());
    restore(undoStack.pop());
    return true;
  }

  public boolean redo() {
    if (redoStack.isEmpty()) {
      return false;
    }
    pendingSnapshot = null;
    undoStack.push(capture());
    restore(redoStack.pop());
    return true;
  }

  private Snapshot capture() {
    Map<String, ModuleState> states = new LinkedHashMap<>();
    for (HudModuleDescriptor descriptor : modules.descriptors()) {
      String id = descriptor.id();
      states.put(id, new ModuleState(modules.enabled(id), modules.settings(id).state()));
    }
    return new Snapshot(states);
  }

  private void restore(Snapshot snapshot) {
    for (Map.Entry<String, ModuleState> entry : snapshot.states().entrySet()) {
      String id = entry.getKey();
      ModuleState state = entry.getValue();
      modules.settings(id).restore(state.settings());
      modules.setEnabled(id, state.enabled());
      modules.settings(id).save();
    }
  }

  private record ModuleState(boolean enabled, HudModuleSettings.State settings) {}

  private record Snapshot(Map<String, ModuleState> states) {
    private Snapshot {
      states = Map.copyOf(states);
    }
  }
}
