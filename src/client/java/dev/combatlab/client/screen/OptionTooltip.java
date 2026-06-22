package dev.combatlab.client.screen;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

import java.time.Duration;

/**
 * Applies consistent cursor-adjacent help text to editor option widgets.
 */
public final class OptionTooltip {
	private static final Duration HOVER_DELAY = Duration.ofMillis(350);
	private static final String INDICATOR = " ⓘ";

	private OptionTooltip() {
	}

	public static <T extends AbstractWidget> T describe(T widget, Component description) {
		widget.setMessage(widget.getMessage().copy().append(INDICATOR));
		widget.setTooltip(Tooltip.create(description));
		widget.setTooltipDelay(HOVER_DELAY);
		return widget;
	}
}
