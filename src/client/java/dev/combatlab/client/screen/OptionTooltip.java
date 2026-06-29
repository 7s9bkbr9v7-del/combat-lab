package dev.combatlab.client.screen;

import java.time.Duration;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

/** Applies consistent cursor-adjacent help text to editor option widgets. */
public final class OptionTooltip {
  private static final Duration HOVER_DELAY = Duration.ofMillis(350);

  private OptionTooltip() {}

  public static <T extends AbstractWidget> T describe(T widget, Component description) {
    widget.setTooltip(Tooltip.create(description));
    widget.setTooltipDelay(HOVER_DELAY);
    return widget;
  }
}
