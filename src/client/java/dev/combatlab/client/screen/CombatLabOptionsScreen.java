package dev.combatlab.client.screen;

import dev.combatlab.client.config.CombatLabOptions;
import dev.combatlab.client.hud.HudModuleDescriptor;
import dev.combatlab.client.hud.HudModuleRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
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
  private final List<DelayedConfirmActionButton> delayedConfirmButtons = new ArrayList<>();

  public CombatLabOptionsScreen(
      Screen parent, CombatLabOptions options, HudModuleRegistry modules) {
    super(parent, Minecraft.getInstance().options, TITLE);
    this.combatLabOptions = options;
    this.modules = modules;
  }

  @Override
  protected void addOptions() {
    delayedConfirmButtons.clear();
    OptionsList optionsList = Objects.requireNonNull(list, "Options list must be initialized");
    optionsList.addHeader(GENERAL_HEADER);
    optionsList.addSmall(
        fullbrightOption(), dynamicFovOption(), debugLoggingOption(), achievementToastOption());

    optionsList.addHeader(HUD_HEADER);
    List<AbstractWidget> moduleRows =
        modules.descriptors().stream()
            .map(this::moduleOptionsRow)
            .map(AbstractWidget.class::cast)
            .toList();
    optionsList.addSmall(moduleRows);
    optionsList.addSmall(
        delayedConfirmButton("options.combatlab.reset_hud_positions", this::resetAllPositions)
            .button(),
        delayedConfirmButton("options.combatlab.reset_config", this::resetAllConfig).button());
  }

  @Override
  public void tick() {
    super.tick();
    delayedConfirmButtons.forEach(DelayedConfirmActionButton::update);
  }

  @Override
  public void onClose() {
    if (list != null) {
      list.applyUnsavedChanges();
    }
    ScreenNavigator.open(minecraft, lastScreen);
  }

  @Override
  public boolean isPauseScreen() {
    return false;
  }

  Screen freshCopy() {
    return new CombatLabOptionsScreen(lastScreen, combatLabOptions, modules);
  }

  private OptionInstance<Boolean> fullbrightOption() {
    return OptionInstance.createBoolean(
        "options.combatlab.fullbright",
        CombatLabOptionWidgets.tooltip("options.combatlab.fullbright.tooltip"),
        combatLabOptions.fullbrightEnabled(),
        combatLabOptions::setFullbrightEnabled);
  }

  private OptionInstance<Boolean> dynamicFovOption() {
    return OptionInstance.createBoolean(
        "options.combatlab.dynamic_fov",
        CombatLabOptionWidgets.tooltip("options.combatlab.dynamic_fov.tooltip"),
        combatLabOptions.dynamicFovEnabled(),
        combatLabOptions::setDynamicFovEnabled);
  }

  private OptionInstance<Boolean> debugLoggingOption() {
    return OptionInstance.createBoolean(
        "options.combatlab.debug_logging",
        combatLabOptions.debugLoggingEnabled(),
        combatLabOptions::setDebugLoggingEnabled);
  }

  private OptionInstance<Boolean> achievementToastOption() {
    return OptionInstance.createBoolean(
        "options.combatlab.achievement_notification",
        CombatLabOptionWidgets.tooltip("options.combatlab.achievement_notification.tooltip"),
        !combatLabOptions.achievementToastsDisabled(),
        selected -> combatLabOptions.setAchievementToastsDisabled(!selected));
  }

  private OptionInstance<Boolean> hudModuleOption(HudModuleDescriptor module) {
    String id = module.id();
    return OptionInstance.createBoolean(
        module.definition().displayName().getString(),
        CombatLabOptionWidgets.tooltip("options.combatlab.hud_module.tooltip"),
        modules.enabled(id),
        selected -> modules.setEnabled(id, selected, width, height));
  }

  private Button moduleOptionsButton(HudModuleDescriptor module) {
    Button button =
        Button.builder(
                Component.literal("..."),
                ignoredButton ->
                    ScreenNavigator.open(
                        minecraft,
                        new HudModuleOptionsScreen(this, modules, module, width, height)))
            .bounds(0, 0, 20, 20)
            .build();
    button.setTooltip(
        Tooltip.create(Component.translatable("options.combatlab.hud_module.options.tooltip")));
    return button;
  }

  private HudModuleOptionsRowWidget moduleOptionsRow(HudModuleDescriptor module) {
    OptionInstance<Boolean> enabledOption = hudModuleOption(module);
    return new HudModuleOptionsRowWidget(
        enabledOption.createButton(options, 0, 0, 150), moduleOptionsButton(module));
  }

  private void resetAllPositions() {
    modules.resetAllPositions();
    delayedConfirmButtons.forEach(DelayedConfirmActionButton::reset);
  }

  private void resetAllConfig() {
    combatLabOptions.resetConfig();
    modules.resetAllConfig(width, height);
    ScreenNavigator.open(minecraft, freshCopy());
  }

  private DelayedConfirmActionButton delayedConfirmButton(String key, Runnable action) {
    DelayedConfirmActionButton button = CombatLabOptionWidgets.delayedConfirmButton(key, action);
    delayedConfirmButtons.add(button);
    return button;
  }
}
