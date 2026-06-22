package dev.combatlab.client.hud;

import dev.combatlab.client.config.CombatLabOptions;
import dev.combatlab.client.debug.DebugLogger;

public abstract class ResizableBaseHudModule extends BaseHudModule implements ResizableHudModule {
	protected ResizableBaseHudModule(
			HudModuleDefinition definition,
			CombatLabOptions options,
			DebugLogger debug
	) {
		super(definition, options, debug);
		if (!definition.resizable()) {
			throw new IllegalArgumentException("Resizable HUD module definition must be marked resizable");
		}
	}

	@Override
	public final HudSize size() {
		double scale = scale();
		HudSize unscaled = unscaledSize();
		return new HudSize(
				(int) Math.ceil(unscaled.width() * scale),
				(int) Math.ceil(unscaled.height() * scale)
		);
	}

	@Override
	public final double scale() {
		return options().hudScale(id().toString());
	}

	@Override
	public final void updateScale(double scale) {
		options().updateHudScale(id().toString(), scale);
	}

	@Override
	public final double minScale() {
		return options().minHudScale();
	}

	@Override
	public final double maxScale() {
		return options().maxHudScale();
	}
}
