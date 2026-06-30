package dev.combatlab.client.feature;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

final class PauseMenuButtonPlacement {
  static final String HUD_EDITOR_KEY = "screen.combatlab.hud_editor";
  static final String OPTIONS_KEY = "menu.options";
  private static final int FULL_WIDTH = 204;
  private static final int COMPACT_SIZE = 20;
  private static final int BUTTON_HEIGHT = 20;
  private static final int SPACING = 4;

  private PauseMenuButtonPlacement() {}

  static Optional<Plan> plan(
      int screenWidth, List<ExistingButton> buttons, boolean modMenuInstalled) {
    if (modMenuInstalled) {
      Optional<Plan> compact = compactPlan(screenWidth, buttons);
      if (compact.isPresent()) {
        return compact;
      }
    }
    return fullWidthPlan(screenWidth, buttons);
  }

  static Optional<Plan> compactPlan(int screenWidth, List<ExistingButton> buttons) {
    return compactAnchor(screenWidth, buttons)
        .flatMap(anchor -> compactPlanForRow(screenWidth, buttons, anchor.y()));
  }

  static Optional<Plan> fullWidthPlan(int screenWidth, List<ExistingButton> buttons) {
    return optionsButton(buttons)
        .or(() -> structuralMenuRowAnchor(screenWidth, buttons))
        .map(
            anchor ->
                new Plan((screenWidth - FULL_WIDTH) / 2, anchor.y(), FULL_WIDTH, BUTTON_HEIGHT));
  }

  private static Optional<ExistingButton> compactAnchor(List<ExistingButton> buttons) {
    Optional<ExistingButton> modMenuButton =
        buttons.stream()
            .filter(PauseMenuButtonPlacement::looksLikeModMenuButton)
            .max(Comparator.comparingInt(ExistingButton::width));
    if (modMenuButton.isPresent()) {
      return modMenuButton;
    }
    return optionsButton(buttons);
  }

  private static Optional<ExistingButton> compactAnchor(
      int screenWidth, List<ExistingButton> buttons) {
    return compactAnchor(buttons).or(() -> structuralMenuRowAnchor(screenWidth, buttons));
  }

  private static Optional<ExistingButton> optionsButton(List<ExistingButton> buttons) {
    return buttons.stream()
        .filter(button -> OPTIONS_KEY.equals(button.translationKey()))
        .min(Comparator.comparingInt(ExistingButton::y));
  }

  private static Optional<Plan> compactPlanForRow(
      int screenWidth, List<ExistingButton> buttons, int rowY) {
    List<ExistingButton> rowButtons =
        buttons.stream().filter(button -> button.overlapsCompactY(rowY)).toList();
    if (rowButtons.isEmpty()) {
      return Optional.empty();
    }

    int top = rowButtons.stream().mapToInt(ExistingButton::y).min().orElse(rowY);
    int right = rowButtons.stream().mapToInt(ExistingButton::right).max().orElse(0);
    Plan rightSide = new Plan(right + SPACING, top, COMPACT_SIZE, COMPACT_SIZE);
    if (rightSide.right() <= screenWidth && doesNotIntersectAny(rightSide, buttons)) {
      return Optional.of(rightSide);
    }

    int left = rowButtons.stream().mapToInt(ExistingButton::x).min().orElse(0);
    Plan leftSide = new Plan(left - SPACING - COMPACT_SIZE, top, COMPACT_SIZE, COMPACT_SIZE);
    if (leftSide.x() >= 0 && doesNotIntersectAny(leftSide, buttons)) {
      return Optional.of(leftSide);
    }

    return Optional.empty();
  }

  private static boolean looksLikeModMenuButton(ExistingButton button) {
    String key = button.translationKey();
    return key != null && (key.equals("modmenu.title") || key.startsWith("modmenu."));
  }

  private static Optional<ExistingButton> structuralMenuRowAnchor(
      int screenWidth, List<ExistingButton> buttons) {
    return buttons.stream()
        .map(button -> structuralRow(screenWidth, buttons, button.y()))
        .filter(Optional::isPresent)
        .map(Optional::orElseThrow)
        .distinct()
        .max(Comparator.comparingInt(StructuralRow::top))
        .map(StructuralRow::anchor);
  }

  private static Optional<StructuralRow> structuralRow(
      int screenWidth, List<ExistingButton> buttons, int rowY) {
    List<ExistingButton> rowButtons =
        buttons.stream().filter(button -> button.overlapsCompactY(rowY)).toList();
    if (rowButtons.size() < 2) {
      return Optional.empty();
    }

    int top = rowButtons.stream().mapToInt(ExistingButton::y).min().orElse(rowY);
    int left = rowButtons.stream().mapToInt(ExistingButton::x).min().orElse(0);
    int right = rowButtons.stream().mapToInt(ExistingButton::right).max().orElse(0);
    int rowWidth = right - left;
    int center = left + rowWidth / 2;
    boolean centered = Math.abs(center - screenWidth / 2) <= COMPACT_SIZE;
    boolean menuWidth = rowWidth >= FULL_WIDTH - SPACING && rowWidth <= FULL_WIDTH + COMPACT_SIZE;
    boolean hasWideButton = rowButtons.stream().anyMatch(button -> button.width() > COMPACT_SIZE);
    if (!centered || !menuWidth || !hasWideButton) {
      return Optional.empty();
    }

    ExistingButton anchor =
        rowButtons.stream().max(Comparator.comparingInt(ExistingButton::width)).orElseThrow();
    return Optional.of(new StructuralRow(top, anchor));
  }

  private static boolean doesNotIntersectAny(Plan plan, List<ExistingButton> buttons) {
    return buttons.stream().noneMatch(button -> button.intersects(plan));
  }

  record ExistingButton(int x, int y, int width, int height, String translationKey) {
    int right() {
      return x + width;
    }

    int bottom() {
      return y + height;
    }

    boolean overlapsCompactY(int otherY) {
      return y < otherY + COMPACT_SIZE && bottom() > otherY;
    }

    boolean intersects(Plan plan) {
      return x < plan.right() && right() > plan.x() && y < plan.bottom() && bottom() > plan.y();
    }
  }

  record Plan(int x, int y, int width, int height) {
    int right() {
      return x + width;
    }

    int bottom() {
      return y + height;
    }
  }

  private record StructuralRow(int top, ExistingButton anchor) {}
}
