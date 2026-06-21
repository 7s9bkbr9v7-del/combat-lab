package dev.combatlab.client.hud;

import dev.combatlab.client.config.CombatLabOptions;
import dev.combatlab.client.debug.DebugLogger;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

abstract class TextHudModule implements HudModule {
	private static final int PADDING = 1;
	private final Identifier id;
	private final Component displayName;
	private final CombatLabOptions options;
	private final DebugLogger debug;
	private String text;
	private HudSize size;

	protected TextHudModule(
			Identifier id,
			Component displayName,
			String initialText,
			double defaultX,
			double defaultY,
			CombatLabOptions options,
			DebugLogger debug
	) {
		this.id = id;
		this.displayName = displayName;
		this.text = initialText;
		this.size = new HudSize(initialText.length() * 6 + PADDING * 2, 9 + PADDING * 2);
		this.options = options;
		this.debug = debug;
		options.ensureHudDefaults(id.toString(), defaultX, defaultY);
	}

	@Override
	public final Identifier id() {
		return id;
	}

	@Override
	public final Component displayName() {
		return displayName;
	}

	@Override
	public final boolean enabled() {
		return options.hudEnabled(id.toString());
	}

	@Override
	public final void setEnabled(boolean enabled) {
		options.setHudEnabled(id.toString(), enabled);
		debug.info("{} {}", displayName.getString(), enabled ? "enabled" : "disabled");
	}

	@Override
	public final HudPosition position(int screenWidth, int screenHeight) {
		return HudLayout.resolve(options.hudX(id.toString()), options.hudY(id.toString()), screenWidth, screenHeight, size);
	}

	@Override
	public final HudSize size() {
		return size;
	}

	@Override
	public final void updatePosition(int x, int y, int screenWidth, int screenHeight) {
		options.updateHudPosition(
				id.toString(),
				HudLayout.normalizeX(x, screenWidth, size),
				HudLayout.normalizeY(y, screenHeight, size)
		);
	}

	@Override
	public final void savePosition() {
		options.save();
		debug.info("{} position saved", displayName.getString());
	}

	@Override
	public final void extractRenderState(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
		Minecraft client = Minecraft.getInstance();
		if (client.player == null || !enabled() || !shouldRenderInGame()) {
			return;
		}
		HudPosition position = position(graphics.guiWidth(), graphics.guiHeight());
		renderAt(graphics, client.font, position);
	}

	@Override
	public final void renderEditorPreview(GuiGraphicsExtractor graphics, Font font) {
		HudPosition position = position(graphics.guiWidth(), graphics.guiHeight());
		renderAt(graphics, font, position);
	}

	protected final void setText(String text) {
		this.text = text;
		Font font = Minecraft.getInstance().font;
		if (font != null) {
			this.size = new HudSize(font.width(text) + PADDING * 2, font.lineHeight + PADDING * 2);
		}
	}

	protected boolean shouldRenderInGame() {
		return true;
	}

	private void renderAt(GuiGraphicsExtractor graphics, Font font, HudPosition position) {
		graphics.text(font, text, position.x() + PADDING, position.y() + PADDING, 0xFFF3F4F6, true);
	}
}
