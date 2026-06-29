package dev.combatlab.client.feature;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class PauseMenuFeatureHooks {
  private static final Component HUD_EDITOR = Component.translatable("screen.combatlab.hud_editor");
  private static final Component HUD_EDITOR_SHORT = Component.literal("H");
  private static final int FULL_WIDTH = 204;
  private static final int COMPACT_SIZE = 20;
  private static final int BUTTON_HEIGHT = 20;
  private static final int SPACING = 4;

  private PauseMenuFeatureHooks() {}

  public static void addHudEditorButton(
      Minecraft minecraft, Screen screen, Consumer<Minecraft> openEditor) {
    if (minecraft == null
        || !(screen instanceof PauseScreen)
        || alreadyHasHudEditorButton(screen)) {
      return;
    }
    hudEditorButton(minecraft, screen, openEditor)
        .ifPresent(button -> Screens.getWidgets(screen).add(button));
  }

  private static Optional<Button> hudEditorButton(
      Minecraft minecraft, Screen screen, Consumer<Minecraft> openEditor) {
    if (modMenuInstalled()) {
      Optional<Button> compactButton = compactButtonBesideModMenu(screen, minecraft, openEditor);
      if (compactButton.isPresent()) {
        return compactButton;
      }
    }
    return fullWidthButton(screen, minecraft, openEditor);
  }

  private static Optional<Button> compactButtonBesideModMenu(
      Screen screen, Minecraft minecraft, Consumer<Minecraft> openEditor) {
    return widgets(screen).stream()
        .filter(PauseMenuFeatureHooks::isModsButton)
        .max(Comparator.comparingInt(AbstractWidget::getWidth))
        .map(
            modsButton ->
                Button.builder(HUD_EDITOR_SHORT, ignoredButton -> openEditor.accept(minecraft))
                    .bounds(
                        modsButton.getRight() + SPACING,
                        modsButton.getY(),
                        COMPACT_SIZE,
                        COMPACT_SIZE)
                    .tooltip(Tooltip.create(HUD_EDITOR))
                    .build());
  }

  private static Optional<Button> fullWidthButton(
      Screen screen, Minecraft minecraft, Consumer<Minecraft> openEditor) {
    Optional<AbstractWidget> optionsButton = optionsButton(screen);
    if (optionsButton.isEmpty()) {
      return Optional.empty();
    }

    AbstractWidget anchor = optionsButton.get();
    int insertY = anchor.getY();
    shiftWidgetsAtOrBelowInsertedButton(screen, insertY);
    return Optional.of(
        Button.builder(HUD_EDITOR, ignoredButton -> openEditor.accept(minecraft))
            .bounds((screen.width - FULL_WIDTH) / 2, insertY, FULL_WIDTH, BUTTON_HEIGHT)
            .build());
  }

  private static Optional<AbstractWidget> optionsButton(Screen screen) {
    return widgets(screen).stream()
        .filter(PauseMenuFeatureHooks::isOptionsButton)
        .min(Comparator.comparingInt(AbstractWidget::getY));
  }

  private static void shiftWidgetsAtOrBelowInsertedButton(Screen screen, int y) {
    for (AbstractWidget widget : widgets(screen)) {
      if (widget.getY() >= y) {
        widget.setY(widget.getY() + BUTTON_HEIGHT + SPACING);
      }
    }
  }

  private static boolean alreadyHasHudEditorButton(Screen screen) {
    return widgets(screen).stream()
        .anyMatch(
            widget ->
                widget.getMessage().getString().equals(HUD_EDITOR.getString())
                    || widget.getMessage().getString().equals(HUD_EDITOR_SHORT.getString()));
  }

  private static boolean isModsButton(AbstractWidget widget) {
    String label = widget.getMessage().getString();
    return label.equals("Mods");
  }

  private static boolean isOptionsButton(AbstractWidget widget) {
    String label = widget.getMessage().getString();
    return label.equals("Options...") || label.equals("Options");
  }

  private static List<AbstractWidget> widgets(Screen screen) {
    return Screens.getWidgets(screen);
  }

  private static boolean modMenuInstalled() {
    return FabricLoader.getInstance().isModLoaded("modmenu");
  }
}
