package dev.combatlab.client.screen;

import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

final class CombatLabOptionWidgets {
  private CombatLabOptionWidgets() {}

  static Button button(String key, String tooltipKey, Runnable action) {
    Button button =
        Button.builder(Component.translatable(key), ignoredButton -> action.run())
            .bounds(0, 0, 150, 20)
            .build();
    button.setTooltip(Tooltip.create(Component.translatable(tooltipKey)));
    return button;
  }

  static DelayedConfirmActionButton delayedConfirmButton(String key, Runnable action) {
    return new DelayedConfirmActionButton(
        Component.translatable(key),
        Component.translatable(key + ".confirm"),
        Component.translatable(key + ".tooltip"),
        action);
  }

  static OptionInstance.TooltipSupplier<Boolean> tooltip(String key) {
    return ignored -> Tooltip.create(Component.translatable(key));
  }
}
