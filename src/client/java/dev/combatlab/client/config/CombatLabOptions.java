package dev.combatlab.client.config;

public final class CombatLabOptions {
	private static final double DEFAULT_HUD_SCALE = 1.0;
	private static final double MIN_HUD_SCALE = 0.5;
	private static final double MAX_HUD_SCALE = 4.0;

	private final CombatLabConfig config;
	private final ConfigStore store;

	private CombatLabOptions(CombatLabConfig config, ConfigStore store) {
		this.config = config;
		this.store = store;
	}

	public static CombatLabOptions load() {
		ConfigStore store = ConfigStore.createDefault();
		return new CombatLabOptions(store.load(), store);
	}

	public boolean debugLoggingEnabled() {
		return config.debugLoggingEnabled;
	}

	public void setDebugLoggingEnabled(boolean enabled) {
		config.debugLoggingEnabled = enabled;
		store.save(config);
	}

	public boolean fullbrightEnabled() {
		return config.fullbrightEnabled;
	}

	public void setFullbrightEnabled(boolean enabled) {
		config.fullbrightEnabled = enabled;
		store.save(config);
	}

	public boolean achievementToastsDisabled() {
		return config.achievementToastsDisabled;
	}

	public void setAchievementToastsDisabled(boolean disabled) {
		config.achievementToastsDisabled = disabled;
		store.save(config);
	}

	public boolean hudEnabled(String id) {
		return module(id).enabled;
	}

	public void setHudEnabled(String id, boolean enabled) {
		module(id).enabled = enabled;
		store.save(config);
	}

	public void ensureHudDefaults(String id, double normalizedX, double normalizedY) {
		if (!config.hudModules.containsKey(id)) {
			HudModuleConfig module = new HudModuleConfig();
			module.normalizedX = clamp(normalizedX);
			module.normalizedY = clamp(normalizedY);
			module.scale = DEFAULT_HUD_SCALE;
			config.hudModules.put(id, module);
		}
	}

	public double hudX(String id) {
		return module(id).normalizedX;
	}

	public double hudY(String id) {
		return module(id).normalizedY;
	}

	public void updateHudPosition(String id, double normalizedX, double normalizedY) {
		HudModuleConfig module = module(id);
		module.normalizedX = clamp(normalizedX);
		module.normalizedY = clamp(normalizedY);
	}

	public double hudScale(String id) {
		return module(id).scale;
	}

	public double minHudScale() {
		return MIN_HUD_SCALE;
	}

	public double maxHudScale() {
		return MAX_HUD_SCALE;
	}

	public void updateHudScale(String id, double scale) {
		module(id).scale = clampScale(scale);
	}

	public void save() {
		store.save(config);
	}

	private HudModuleConfig module(String id) {
		HudModuleConfig module = config.hudModules.computeIfAbsent(id, ignored -> new HudModuleConfig());
		if (module.scale <= 0.0) {
			module.scale = DEFAULT_HUD_SCALE;
		} else {
			module.scale = clampScale(module.scale);
		}
		return module;
	}

	private static double clamp(double value) {
		return Math.clamp(value, 0.0, 1.0);
	}

	private static double clampScale(double value) {
		return Math.clamp(value, MIN_HUD_SCALE, MAX_HUD_SCALE);
	}
}
