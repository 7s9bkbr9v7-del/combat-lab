package dev.combatlab.client.screen;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public final class OptionHelpHint {
	private static final Component TEXT = Component.literal("ⓘ means you can hover for more information");
	private static final int MARGIN = 8;
	private static final int COLOR = 0xFF9CA3AF;

	private OptionHelpHint() {
	}

	public static void render(GuiGraphicsExtractor graphics, Font font, int screenWidth, int screenHeight) {
		int x = Math.max(MARGIN, screenWidth - font.width(TEXT) - MARGIN);
		int y = Math.max(MARGIN, screenHeight - font.lineHeight - MARGIN);
		graphics.text(font, TEXT, x, y, COLOR, true);
	}
}
