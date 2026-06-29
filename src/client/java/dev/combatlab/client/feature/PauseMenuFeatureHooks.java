package dev.combatlab.client.feature;

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
import net.minecraft.network.chat.contents.TranslatableContents;

public final class PauseMenuFeatureHooks {
  private static final Component HUD_EDITOR = Component.translatable("screen.combatlab.hud_editor");
  private static final Component HUD_EDITOR_SHORT = Component.literal("H");
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
    return PauseMenuButtonPlacement.plan(screen.width, existingButtons(screen), modMenuInstalled())
        .map(
            plan -> {
              shiftForInsertedButton(screen, plan);
              return buttonFromPlan(minecraft, openEditor, plan);
            });
  }

  private static Button buttonFromPlan(
      Minecraft minecraft, Consumer<Minecraft> openEditor, PauseMenuButtonPlacement.Plan plan) {
    if (plan.width() == 20 && plan.height() == 20) {
      return Button.builder(HUD_EDITOR_SHORT, ignoredButton -> openEditor.accept(minecraft))
          .bounds(plan.x(), plan.y(), plan.width(), plan.height())
          .tooltip(Tooltip.create(HUD_EDITOR))
          .build();
    }
    return Button.builder(HUD_EDITOR, ignoredButton -> openEditor.accept(minecraft))
        .bounds(plan.x(), plan.y(), plan.width(), plan.height())
        .build();
  }

  private static void shiftForInsertedButton(Screen screen, PauseMenuButtonPlacement.Plan plan) {
    if (plan.width() == 20 && plan.height() == 20) {
      return;
    }
    shiftWidgetsAtOrBelowInsertedButton(screen, plan.y());
  }

  private static void shiftWidgetsAtOrBelowInsertedButton(Screen screen, int y) {
    for (AbstractWidget widget : widgets(screen)) {
      if (widget.getY() >= y) {
        widget.setY(widget.getY() + planVerticalSpace());
      }
    }
  }

  private static int planVerticalSpace() {
    return 20 + SPACING;
  }

  private static boolean alreadyHasHudEditorButton(Screen screen) {
    return widgets(screen).stream()
        .anyMatch(
            widget ->
                messageKey(widget)
                        .map(PauseMenuButtonPlacement.HUD_EDITOR_KEY::equals)
                        .orElse(false)
                    || widget.getMessage().getString().equals(HUD_EDITOR_SHORT.getString()));
  }

  private static List<PauseMenuButtonPlacement.ExistingButton> existingButtons(Screen screen) {
    return widgets(screen).stream()
        .map(
            widget ->
                new PauseMenuButtonPlacement.ExistingButton(
                    widget.getX(),
                    widget.getY(),
                    widget.getWidth(),
                    widget.getHeight(),
                    messageKey(widget).orElse(null)))
        .toList();
  }

  private static Optional<String> messageKey(AbstractWidget widget) {
    if (widget.getMessage().getContents() instanceof TranslatableContents contents) {
      return Optional.of(contents.getKey());
    }
    return Optional.empty();
  }

  private static List<AbstractWidget> widgets(Screen screen) {
    return Screens.getWidgets(screen);
  }

  private static boolean modMenuInstalled() {
    return FabricLoader.getInstance().isModLoaded("modmenu");
  }
}
