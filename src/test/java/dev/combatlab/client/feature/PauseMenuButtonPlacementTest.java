package dev.combatlab.client.feature;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class PauseMenuButtonPlacementTest {
  @Test
  void fullWidthButtonAnchorsToLocalizedOptionsKey() {
    PauseMenuButtonPlacement.Plan plan =
        PauseMenuButtonPlacement.plan(
                400,
                List.of(
                    button(98, 72, 204, "menu.returnToGame"),
                    button(98, 120, 98, PauseMenuButtonPlacement.OPTIONS_KEY)),
                false)
            .orElseThrow();

    assertEquals(new PauseMenuButtonPlacement.Plan(98, 120, 204, 20), plan);
  }

  @Test
  void compactButtonUsesModMenuTranslationKeyWithoutRenderedEnglish() {
    PauseMenuButtonPlacement.Plan plan =
        PauseMenuButtonPlacement.plan(
                400,
                List.of(
                    button(98, 120, 98, PauseMenuButtonPlacement.OPTIONS_KEY),
                    button(200, 120, 98, "modmenu.title")),
                true)
            .orElseThrow();

    assertEquals(new PauseMenuButtonPlacement.Plan(302, 120, 20, 20), plan);
  }

  @Test
  void compactButtonExtendsOptionsRowForIconOnlyModMenuVariant() {
    PauseMenuButtonPlacement.Plan plan =
        PauseMenuButtonPlacement.plan(
                400,
                List.of(
                    button(98, 120, 98, PauseMenuButtonPlacement.OPTIONS_KEY),
                    button(200, 120, 98, "menu.feedback"),
                    button(302, 120, 20, null)),
                true)
            .orElseThrow();

    assertEquals(new PauseMenuButtonPlacement.Plan(326, 120, 20, 20), plan);
  }

  @Test
  void compactButtonFallsBackToLeftSideWhenRowRightSideDoesNotFit() {
    PauseMenuButtonPlacement.Plan plan =
        PauseMenuButtonPlacement.plan(
                390,
                List.of(
                    button(278, 120, 98, PauseMenuButtonPlacement.OPTIONS_KEY),
                    button(176, 120, 98, "menu.feedback")),
                true)
            .orElseThrow();

    assertEquals(new PauseMenuButtonPlacement.Plan(152, 120, 20, 20), plan);
  }

  @Test
  void noButtonIsAddedWhenThereIsNoDurableAnchor() {
    assertTrue(
        PauseMenuButtonPlacement.plan(
                400, List.of(button(98, 120, 204, "menu.returnToGame")), false)
            .isEmpty());
  }

  private static PauseMenuButtonPlacement.ExistingButton button(
      int x, int y, int width, String key) {
    return new PauseMenuButtonPlacement.ExistingButton(x, y, width, 20, key);
  }
}
