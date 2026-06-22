package dev.combatlab.client.hud;

import dev.combatlab.client.config.CombatLabOptions;
import dev.combatlab.client.debug.DebugLogger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

abstract class TextHudModule extends ResizableBaseHudModule {
	private static final int PADDING = 1;
	private String text;
	private HudSize unscaledSize;

	protected TextHudModule(
			HudModuleDefinition definition,
			String initialText,
			CombatLabOptions options,
			DebugLogger debug
	) {
		super(definition, options, debug);
		this.text = initialText;
		this.unscaledSize = new HudSize(initialText.length() * 6 + PADDING * 2, 9 + PADDING * 2);
	}

	@Override
	public final HudSize unscaledSize() {
		return unscaledSize;
	}

	protected final void setText(String text) {
		this.text = text;
		Font font = Minecraft.getInstance().font;
		if (font != null) {
			this.unscaledSize = new HudSize(font.width(text) + PADDING * 2, font.lineHeight + PADDING * 2);
		}
	}

	@Override
	protected final void renderModule(GuiGraphicsExtractor graphics, HudRenderContext context) {
		HudRectangle bounds = context.bounds();
		double scale = scale();
		if (scale == 1.0) {
			graphics.text(context.font(), text, bounds.x() + PADDING, bounds.y() + PADDING, 0xFFF3F4F6, true);
			return;
		}

		graphics.pose().pushMatrix();
		graphics.pose().translate(bounds.x(), bounds.y());
		graphics.pose().scale((float) scale, (float) scale);
		graphics.text(context.font(), text, PADDING, PADDING, 0xFFF3F4F6, true);
		graphics.pose().popMatrix();
	}
}
