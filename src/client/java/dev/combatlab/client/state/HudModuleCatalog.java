package dev.combatlab.client.state;

import java.util.List;

public record HudModuleCatalog(List<Module> modules) {
  public HudModuleCatalog {
    modules = List.copyOf(modules);
  }

  public record Module(
      String id,
      String displayName,
      double defaultX,
      double defaultY,
      boolean resizable,
      boolean loadWhenDisabled) {}
}
