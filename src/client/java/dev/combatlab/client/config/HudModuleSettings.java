package dev.combatlab.client.config;

public final class HudModuleSettings {
	public static final double MIN_SCALE = 0.5;
	public static final double MAX_SCALE = 4.0;

	private final HudModuleConfig config;
	private final Runnable saveAction;

	HudModuleSettings(HudModuleConfig config, Runnable saveAction) {
		this.config = config;
		this.saveAction = saveAction;
		config.scale = clampScale(config.scale);
	}

	public boolean enabled() {
		return config.enabled;
	}

	public void setEnabled(boolean enabled) {
		config.enabled = enabled;
		save();
	}

	public double normalizedX() {
		return config.normalizedX;
	}

	public double normalizedY() {
		return config.normalizedY;
	}

	public void updatePosition(double normalizedX, double normalizedY) {
		config.normalizedX = clampPosition(normalizedX);
		config.normalizedY = clampPosition(normalizedY);
	}

	public double scale() {
		return config.scale;
	}

	public void updateScale(double scale) {
		config.scale = clampScale(scale);
	}

	public String layout() {
		return config.layout;
	}

	public void updateLayout(String layout) {
		config.layout = layout;
	}

	public void save() {
		saveAction.run();
	}

	private static double clampPosition(double value) {
		return Math.clamp(value, 0.0, 1.0);
	}

	private static double clampScale(double value) {
		if (value <= 0.0) {
			return 1.0;
		}
		return Math.clamp(value, MIN_SCALE, MAX_SCALE);
	}
}
