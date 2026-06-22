package dev.combatlab.client.hud;

import dev.combatlab.client.config.CombatLabOptions;
import dev.combatlab.client.config.HudModuleSettings;
import dev.combatlab.client.debug.DebugLogger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public abstract class BaseHudModule implements HudModule {
	private final HudModuleDefinition definition;
	private final HudModuleSettings settings;
	private final DebugLogger debug;

	protected BaseHudModule(HudModuleDefinition definition, CombatLabOptions options, DebugLogger debug) {
		this.definition = definition;
		this.settings = options.bindHudModule(
				definition.id().toString(),
				definition.defaultX(),
				definition.defaultY()
		);
		this.debug = debug;
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
		return settings.enabled();
	}

	@Override
	public final void setEnabled(boolean enabled) {
		settings.setEnabled(enabled);
		debug.info("{} {}", displayName().getString(), enabled ? "enabled" : "disabled");
	}

	@Override
	public final HudPosition position(int screenWidth, int screenHeight) {
		return HudLayout.resolve(
				settings.normalizedX(),
				settings.normalizedY(),
				screenWidth,
				screenHeight,
				size()
		);
	}

	@Override
	public final HudOrientation orientation(int screenWidth, int screenHeight) {
		return HudOrientationResolver.resolve(bounds(screenWidth, screenHeight), screenWidth, screenHeight);
	}

	@Override
	public final void updatePosition(int x, int y, int screenWidth, int screenHeight) {
		HudSize size = size();
		settings.updatePosition(
				HudLayout.normalizeX(x, screenWidth, size),
				HudLayout.normalizeY(y, screenHeight, size)
		);
	}

	@Override
	public final void savePosition() {
		settings.save();
		debug.info("{} position saved", displayName().getString());
	}

	@Override
	public final void renderInGame(GuiGraphicsExtractor graphics, HudRenderContext context) {
		if (!shouldRenderInGame(context)) {
			return;
		}
		renderModule(graphics, context);
	}

	@Override
	public final void renderEditorPreview(GuiGraphicsExtractor graphics, Font font, HudRectangle bounds) {
		renderModule(graphics, new HudRenderContext(Minecraft.getInstance(), font, bounds));
	}

	protected final HudModuleSettings settings() {
		return settings;
	}

	protected final DebugLogger debug() {
		return debug;
	}

	protected boolean shouldRenderInGame(HudRenderContext context) {
		return true;
	}

	protected abstract void renderModule(GuiGraphicsExtractor graphics, HudRenderContext context);

	@Override
	public final HudRectangle bounds(int screenWidth, int screenHeight) {
		HudSize size = size();
		HudPosition position = HudLayout.resolve(
				settings.normalizedX(),
				settings.normalizedY(),
				screenWidth,
				screenHeight,
				size
		);
		return new HudRectangle(position.x(), position.y(), size.width(), size.height());
	}
}
