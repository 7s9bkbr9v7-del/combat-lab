package dev.combatlab.client.hud;

@FunctionalInterface
public interface HudModuleFactory {
  HudModule create(HudModuleDependencies dependencies);
}
