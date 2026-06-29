package dev.combatlab.client.screen;

import dev.combatlab.client.config.CombatLabOptions;
import dev.combatlab.client.debug.DebugLogger;
import dev.combatlab.client.feature.AchievementToastController;
import dev.combatlab.client.feature.DynamicFovController;
import dev.combatlab.client.feature.FullbrightController;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class GeneralOptionsScreen extends Screen {
  private static final Component TITLE = Component.literal("General Options");
  private static final Component FULLBRIGHT_DESCRIPTION =
      Component.literal("Brightens dark areas without applying a potion effect.");
  private static final Component DYNAMIC_FOV_DESCRIPTION =
      Component.literal(
          "Controls movement-based FOV changes, including sprinting and speed effects.");
  private final Screen parent;
  private final CombatLabOptions options;
  private final DebugLogger debug;

  public GeneralOptionsScreen(Screen parent, CombatLabOptions options, DebugLogger debug) {
    super(TITLE);
    this.parent = parent;
    this.options = options;
    this.debug = debug;
  }

  @Override
  protected void init() {
    int left = width / 2 - 100;
    int y = height / 2 - 35;
    addRenderableWidget(
        OptionTooltip.describe(
            Checkbox.builder(Component.literal("Fullbright"), font)
                .pos(left, y)
                .maxWidth(200)
                .selected(options.fullbrightEnabled())
                .onValueChange(
                    (checkbox, selected) -> {
                      options.setFullbrightEnabled(selected);
                      FullbrightController.setEnabled(selected);
                      debug.info("Fullbright {}", selected ? "enabled" : "disabled");
                    })
                .build(),
            FULLBRIGHT_DESCRIPTION));

    addRenderableWidget(
        OptionTooltip.describe(
            Checkbox.builder(Component.literal("Dynamic FOV"), font)
                .pos(left, y + 30)
                .maxWidth(200)
                .selected(options.dynamicFovEnabled())
                .onValueChange(
                    (checkbox, selected) -> {
                      options.setDynamicFovEnabled(selected);
                      DynamicFovController.setEnabled(selected);
                      debug.info("Dynamic FOV {}", selected ? "enabled" : "disabled");
                    })
                .build(),
            DYNAMIC_FOV_DESCRIPTION));

    addRenderableWidget(
        Checkbox.builder(Component.literal("Debug logging"), font)
            .pos(left, y + 60)
            .maxWidth(200)
            .selected(options.debugLoggingEnabled())
            .onValueChange(
                (checkbox, selected) -> {
                  options.setDebugLoggingEnabled(selected);
                  debug.announce(selected);
                })
            .build());

    addRenderableWidget(
        Checkbox.builder(Component.literal("Disable achievement notifications"), font)
            .pos(left, y + 90)
            .maxWidth(200)
            .selected(options.achievementToastsDisabled())
            .onValueChange(
                (checkbox, selected) -> {
                  options.setAchievementToastsDisabled(selected);
                  AchievementToastController.setDisabled(selected);
                  debug.info("Achievement notifications {}", selected ? "disabled" : "enabled");
                })
            .build());

    addRenderableWidget(
        Button.builder(Component.literal("Done"), button -> onClose())
            .bounds(left, y + 135, 200, 20)
            .build());
  }

  @Override
  public void extractRenderState(
      GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
    super.extractRenderState(graphics, mouseX, mouseY, partialTick);
    graphics.centeredText(font, title, width / 2, 30, 0xFFFFFFFF);
  }

  @Override
  public void onClose() {
    ScreenNavigator.open(minecraft, parent);
  }

  @Override
  public boolean isPauseScreen() {
    return false;
  }
}
