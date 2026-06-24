package dev.combatlab.client.hud;

import dev.combatlab.client.config.CombatLabOptions;
import dev.combatlab.client.config.HudModuleSettings;
import dev.combatlab.client.debug.DebugLogger;
import dev.combatlab.client.state.ClientGameState;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.function.Function;

public abstract class BaseHudModule implements HudModule {
	private final HudModuleDefinition definition;
	private final HudModuleSettings settings;
	private final DebugLogger debug;
	private Function<String, HudModule> moduleLookup = ignored -> null;
	private boolean resolvingBounds;

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
		HudRectangle bounds = bounds(screenWidth, screenHeight);
		return new HudPosition(bounds.x(), bounds.y());
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
	public final String attachmentTargetId() {
		return settings.attachedTo();
	}

	@Override
	public final void attachTo(HudModule target, HudAttachmentSide side, int offset) {
		settings.updateAttachment(target.id().toString(), side.name(), offset);
	}

	@Override
	public final void clearAttachment() {
		settings.clearAttachment();
	}

	@Override
	public final void detach(int screenWidth, int screenHeight) {
		if (settings.attachedTo() == null) {
			return;
		}
		HudRectangle currentBounds = bounds(screenWidth, screenHeight);
		clearAttachment();
		updatePosition(currentBounds.x(), currentBounds.y(), screenWidth, screenHeight);
	}

	@Override
	public final void renderInGame(GuiGraphicsExtractor graphics, HudRenderContext context) {
		if (!shouldRenderInGame(context)) {
			return;
		}
		renderModule(graphics, context);
	}

	@Override
	public final void renderEditorPreview(
			GuiGraphicsExtractor graphics,
			Font font,
			HudRectangle bounds,
			int screenWidth,
			int screenHeight,
			ClientGameState gameState
	) {
		renderModule(graphics, new HudRenderContext(
				font,
				bounds,
				screenWidth,
				screenHeight,
				true,
				gameState.withHud(gameState.hud().forEditorPreview())
		));
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
		HudPosition fallback = HudLayout.resolve(
				settings.normalizedX(),
				settings.normalizedY(),
				screenWidth,
				screenHeight,
				size
		);
		HudPosition attached = attachedPosition(size, screenWidth, screenHeight);
		HudPosition position = attached != null ? attached : fallback;
		return new HudRectangle(position.x(), position.y(), size.width(), size.height());
	}

	void bindModuleLookup(Function<String, HudModule> moduleLookup) {
		this.moduleLookup = moduleLookup;
	}

	private HudPosition attachedPosition(
			HudSize size,
			int screenWidth,
			int screenHeight
	) {
		if (resolvingBounds) {
			return null;
		}

		String targetId = settings.attachedTo();
		HudAttachmentSide side = HudAttachmentSide.fromStored(settings.attachmentSide());
		HudModule target = targetId == null ? null : moduleLookup.apply(targetId);
		if (target == null || target == this || side == null) {
			return null;
		}

		resolvingBounds = true;
		try {
			return side.resolve(target.bounds(screenWidth, screenHeight), size, settings.attachmentOffset());
		} finally {
			resolvingBounds = false;
		}
	}
}
