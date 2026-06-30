package dev.combatlab.client.screen;

import dev.combatlab.client.config.CombatLabOptions;
import dev.combatlab.client.debug.DebugLogger;
import dev.combatlab.client.feature.AchievementToastController;
import dev.combatlab.client.feature.DynamicFovController;
import dev.combatlab.client.feature.FullbrightController;
import dev.combatlab.client.hud.HudModuleDescriptor;
import dev.combatlab.client.hud.HudModuleRegistry;
import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.Component;

public final class CombatLabOptionsScreen extends OptionsSubScreen {
  private static final Component TITLE = Component.translatable("options.combatlab.title");
  private static final Component GENERAL_HEADER =
      Component.translatable("options.combatlab.section.general");
  private static final Component HUD_HEADER =
      Component.translatable("options.combatlab.section.hud");
  private final CombatLabOptions combatLabOptions;
  private final HudModuleRegistry modules;
  private final DebugLogger debug;

  public CombatLabOptionsScreen(
      Screen parent, CombatLabOptions options, HudModuleRegistry modules, DebugLogger debug) {
    super(parent, Minecraft.getInstance().options, TITLE);
    this.combatLabOptions = options;
    this.modules = modules;
    this.debug = debug;
  }

  @Override
  protected void addOptions() {
    OptionsList optionsList = Objects.requireNonNull(list, "Options list must be initialized");
    optionsList.addHeader(GENERAL_HEADER);
    optionsList.addSmall(
        fullbrightOption(), dynamicFovOption(), debugLoggingOption(), achievementToastOption());

    optionsList.addHeader(HUD_HEADER);
    OptionInstance<?>[] hudOptions =
        modules.descriptors().stream().map(this::hudModuleOption).toArray(OptionInstance<?>[]::new);
    optionsList.addSmall(hudOptions);
  }

  @Override
  public void onClose() {
    ScreenNavigator.open(minecraft, lastScreen);
  }

  @Override
  public boolean isPauseScreen() {
    return false;
  }

  private OptionInstance<Boolean> fullbrightOption() {
    return OptionInstance.createBoolean(
        "options.combatlab.fullbright",
        tooltip("options.combatlab.fullbright.tooltip"),
        combatLabOptions.fullbrightEnabled(),
        selected -> {
          combatLabOptions.setFullbrightEnabled(selected);
          FullbrightController.setEnabled(selected);
          debug.info("Fullbright {}", selected ? "enabled" : "disabled");
        });
  }

  private OptionInstance<Boolean> dynamicFovOption() {
    return OptionInstance.createBoolean(
        "options.combatlab.dynamic_fov",
        tooltip("options.combatlab.dynamic_fov.tooltip"),
        combatLabOptions.dynamicFovEnabled(),
        selected -> {
          combatLabOptions.setDynamicFovEnabled(selected);
          DynamicFovController.setEnabled(selected);
          debug.info("Dynamic FOV {}", selected ? "enabled" : "disabled");
        });
  }

  private OptionInstance<Boolean> debugLoggingOption() {
    return OptionInstance.createBoolean(
        "options.combatlab.debug_logging",
        combatLabOptions.debugLoggingEnabled(),
        selected -> {
          combatLabOptions.setDebugLoggingEnabled(selected);
          debug.announce(selected);
        });
  }

  private OptionInstance<Boolean> achievementToastOption() {
    return OptionInstance.createBoolean(
        "options.combatlab.disable_achievement_notifications",
        combatLabOptions.achievementToastsDisabled(),
        selected -> {
          combatLabOptions.setAchievementToastsDisabled(selected);
          AchievementToastController.setDisabled(selected);
          debug.info("Achievement notifications {}", selected ? "disabled" : "enabled");
        });
  }

  private OptionInstance<Boolean> hudModuleOption(HudModuleDescriptor module) {
    String id = module.id();
    return OptionInstance.createBoolean(
        module.definition().displayName().getString(),
        tooltip("options.combatlab.hud_module.tooltip"),
        modules.enabled(id),
        selected -> modules.setEnabled(id, selected));
  }

  private static OptionInstance.TooltipSupplier<Boolean> tooltip(String key) {
    return ignored -> Tooltip.create(Component.translatable(key));
  }
}
