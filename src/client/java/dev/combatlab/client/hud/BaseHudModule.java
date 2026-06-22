package dev.combatlab.client.hud;

import dev.combatlab.client.config.CombatLabOptions;
import dev.combatlab.client.debug.DebugLogger;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public abstract class BaseHudModule implements HudModule {
	private final HudModuleDefinition definition;
	private final CombatLabOptions options;
	private final DebugLogger debug;

	protected BaseHudModule(HudModuleDefinition definition, CombatLabOptions options, DebugLogger debug) {
		this.definition = definition;
		this.options = options;
		this.debug = debug;
		options.ensureHudDefaults(definition.id().toString(), definition.defaultX(), definition.defaultY());
	}

	@Override
	public final Identifier id() {
		return definition.id();
	}

	@Override
	public final Component displayName() {
		return definition.displayName();
	}

	public final HudModuleDefinition definition() {
		return definition;
	}

	@Override
	public final boolean enabled() {
		return options.hudEnabled(configId());
	}

	@Override
	public final void setEnabled(boolean enabled) {
		options.setHudEnabled(configId(), enabled);
		debug.info("{} {}", displayName().getString(), enabled ? "enabled" : "disabled");
	}

	@Override
	public final HudPosition position(int screenWidth, int screenHeight) {
		return HudLayout.resolve(options.hudX(configId()), options.hudY(configId()), screenWidth, screenHeight, size());
	}

	@Override
	public final HudOrientation orientation(int screenWidth, int screenHeight) {
		return HudOrientationResolver.resolve(bounds(screenWidth, screenHeight), screenWidth, screenHeight);
	}

	@Override
	public final void updatePosition(int x, int y, int screenWidth, int screenHeight) {
		HudSize size = size();
		options.updateHudPosition(
				configId(),
				HudLayout.normalizeX(x, screenWidth, size),
				HudLayout.normalizeY(y, screenHeight, size)
		);
	}

	@Override
	public final void savePosition() {
		options.save();
		debug.info("{} position saved", displayName().getString());
	}

	@Override
	public final void extractRenderState(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
		Minecraft client = Minecraft.getInstance();
		HudRenderContext context = renderContext(client, client.font, graphics.guiWidth(), graphics.guiHeight(), false);
		if (client.player == null || !enabled() || !shouldRenderInGame(context)) {
			return;
		}
		renderModule(graphics, context);
	}

	@Override
	public final void renderEditorPreview(GuiGraphicsExtractor graphics, Font font) {
		Minecraft client = Minecraft.getInstance();
		renderModule(graphics, renderContext(client, font, graphics.guiWidth(), graphics.guiHeight(), true));
	}

	protected final CombatLabOptions options() {
		return options;
	}

	protected final DebugLogger debug() {
		return debug;
	}

	protected boolean shouldRenderInGame(HudRenderContext context) {
		return true;
	}

	protected abstract void renderModule(GuiGraphicsExtractor graphics, HudRenderContext context);

	private HudRenderContext renderContext(
			Minecraft client,
			Font font,
			int screenWidth,
			int screenHeight,
			boolean editorPreview
	) {
		HudRectangle bounds = bounds(screenWidth, screenHeight);
		return new HudRenderContext(
				client,
				font,
				bounds,
				HudOrientationResolver.resolve(bounds, screenWidth, screenHeight),
				editorPreview
		);
	}

	private HudRectangle bounds(int screenWidth, int screenHeight) {
		HudPosition position = position(screenWidth, screenHeight);
		HudSize size = size();
		return new HudRectangle(position.x(), position.y(), size.width(), size.height());
	}

	private String configId() {
		return definition.id().toString();
	}
}
