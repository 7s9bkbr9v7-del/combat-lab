package dev.combatlab.client.screen.hudeditor;

import dev.combatlab.client.config.CombatLabOptions;
import dev.combatlab.client.debug.DebugLogger;
import dev.combatlab.client.hud.HudModule;
import dev.combatlab.client.hud.HudModuleDescriptor;
import dev.combatlab.client.hud.HudModuleRegistry;
import dev.combatlab.client.screen.CombatLabOptionsScreen;
import dev.combatlab.client.screen.HudModuleOptionsScreen;
import dev.combatlab.client.screen.ScreenNavigator;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class HudOptionsNavigation {
  private final CombatLabOptions options;
  private final HudModuleRegistry modules;
  private final DebugLogger debug;

  public HudOptionsNavigation(
      CombatLabOptions options, HudModuleRegistry modules, DebugLogger debug) {
    this.options = options;
    this.modules = modules;
    this.debug = debug;
  }

  public List<Button> createButtons(
      Screen editor, Minecraft minecraft, int screenWidth, int screenHeight, Runnable closeEditor) {
    Button optionsButton =
        Button.builder(
                Component.literal("HUD Options"),
                ignoredButton -> {
                  debug.info("Opening HUD options screen");
                  ScreenNavigator.open(
                      minecraft, new CombatLabOptionsScreen(editor, options, modules));
                })
            .bounds(screenWidth / 2 - 75, screenHeight / 2 - 10, 150, 20)
            .build();

    Button doneButton =
        Button.builder(Component.literal("Done"), ignoredButton -> closeEditor.run())
            .bounds(screenWidth / 2 - 75, screenHeight - 35, 150, 20)
            .build();
    return List.of(optionsButton, doneButton);
  }

  public void openModuleOptions(
      Screen editor, Minecraft minecraft, HudModule module, int screenWidth, int screenHeight) {
    HudModuleDescriptor descriptor = descriptor(module);
    if (descriptor == null) {
      return;
    }
    debug.info("Opening {} HUD options screen", module.displayName().getString());
    ScreenNavigator.open(
        minecraft,
        new HudModuleOptionsScreen(editor, modules, descriptor, screenWidth, screenHeight));
  }

  private HudModuleDescriptor descriptor(HudModule module) {
    String id = module.id().toString();
    for (HudModuleDescriptor descriptor : modules.descriptors()) {
      if (descriptor.id().equals(id)) {
        return descriptor;
      }
    }
    return null;
  }
}
