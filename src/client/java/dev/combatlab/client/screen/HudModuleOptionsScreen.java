package dev.combatlab.client.screen;

import dev.combatlab.client.config.HudModuleSettings;
import dev.combatlab.client.hud.HudModuleDescriptor;
import dev.combatlab.client.hud.HudModuleRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.Component;

public final class HudModuleOptionsScreen extends OptionsSubScreen {
  private final HudModuleRegistry modules;
  private final HudModuleDescriptor module;
  private final int editorWidth;
  private final int editorHeight;
  private final List<DelayedConfirmActionButton> delayedConfirmButtons = new ArrayList<>();

  public HudModuleOptionsScreen(
      Screen parent,
      HudModuleRegistry modules,
      HudModuleDescriptor module,
      int editorWidth,
      int editorHeight) {
    super(
        parent,
        Minecraft.getInstance().options,
        Component.literal(module.definition().displayName().getString() + " Options"));
    this.modules = modules;
    this.module = module;
    this.editorWidth = editorWidth;
    this.editorHeight = editorHeight;
  }

  @Override
  protected void addOptions() {
    delayedConfirmButtons.clear();
    OptionsList optionsList = Objects.requireNonNull(list, "Options list must be initialized");
    optionsList.addSmall(enabledOption(), scaleOption());
    optionsList.addSmall(textColorOption(), resetColorButton());
    optionsList.addSmall(
        CombatLabOptionWidgets.button(
            "options.combatlab.hud_module.reset_position",
            "options.combatlab.hud_module.reset_position.tooltip",
            this::resetPosition),
        delayedConfirmButton("options.combatlab.hud_module.reset_config", this::resetConfig)
            .button());
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
    Screen target =
        lastScreen instanceof CombatLabOptionsScreen optionsScreen
            ? optionsScreen.freshCopy()
            : lastScreen;
    ScreenNavigator.open(minecraft, target);
  }

  @Override
  public boolean isPauseScreen() {
    return false;
  }

  private OptionInstance<Boolean> enabledOption() {
    return OptionInstance.createBoolean(
        "options.combatlab.hud_module.enabled",
        CombatLabOptionWidgets.tooltip("options.combatlab.hud_module.tooltip"),
        modules.enabled(module.id()),
        enabled -> modules.setEnabled(module.id(), enabled, editorWidth, editorHeight));
  }

  private OptionInstance<Integer> scaleOption() {
    HudModuleSettings settings = settings();
    return new OptionInstance<>(
        "options.combatlab.hud_module.scale",
        OptionInstance.noTooltip(),
        (caption, value) -> Component.literal(caption.getString() + ": " + value + "%"),
        new OptionInstance.IntRange(
            HudModuleSettings.displayPercent(HudModuleSettings.MIN_SCALE),
            HudModuleSettings.displayPercent(HudModuleSettings.MAX_SCALE),
            true),
        HudModuleSettings.displayPercent(settings.scale()),
        value -> {
          settings.updateScale(value * HudModuleSettings.DEFAULT_SCALE / 100.0D);
          settings.save();
        });
  }

  private ColorSwatchOptionWidget textColorOption() {
    return new ColorSwatchOptionWidget(
        Component.translatable("options.combatlab.hud_module.text_color"),
        settings()::textColorRgb,
        () ->
            ScreenNavigator.open(
                minecraft,
                new ColorPickerScreen(
                    this,
                    Component.translatable("options.combatlab.hud_module.text_color"),
                    textColorSetting())));
  }

  private ColorSetting textColorSetting() {
    return new ColorSetting() {
      @Override
      public int colorRgb() {
        return settings().textColorRgb();
      }

      @Override
      public void updateColorRgb(int colorRgb) {
        HudModuleSettings settings = settings();
        settings.updateTextColor(colorRgb);
        settings.save();
      }
    };
  }

  private net.minecraft.client.gui.components.Button resetColorButton() {
    return CombatLabOptionWidgets.button(
        "options.combatlab.hud_module.reset_color",
        "options.combatlab.hud_module.reset_color.tooltip",
        this::resetColor);
  }

  private void resetColor() {
    HudModuleSettings settings = settings();
    settings.updateTextColor(HudModuleSettings.DEFAULT_TEXT_COLOR);
    settings.save();
    delayedConfirmButtons.forEach(DelayedConfirmActionButton::reset);
  }

  private void resetPosition() {
    HudModuleSettings settings = settings();
    settings.resetPosition(module.definition().defaultX(), module.definition().defaultY());
    settings.save();
    delayedConfirmButtons.forEach(DelayedConfirmActionButton::reset);
  }

  private void resetConfig() {
    modules.setEnabled(module.id(), false, editorWidth, editorHeight);
    settings().reset(module.definition().defaultX(), module.definition().defaultY());
    settings().save();
    ScreenNavigator.open(
        minecraft,
        new HudModuleOptionsScreen(lastScreen, modules, module, editorWidth, editorHeight));
  }

  private HudModuleSettings settings() {
    return modules.settings(module.id());
  }

  private DelayedConfirmActionButton delayedConfirmButton(String key, Runnable action) {
    DelayedConfirmActionButton button = CombatLabOptionWidgets.delayedConfirmButton(key, action);
    delayedConfirmButtons.add(button);
    return button;
  }
}
