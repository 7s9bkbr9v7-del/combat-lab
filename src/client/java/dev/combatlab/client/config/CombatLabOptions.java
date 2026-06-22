package dev.combatlab.client.config;

import java.util.HashMap;
import java.util.Map;

public final class CombatLabOptions {
	private final CombatLabConfig config;
	private final ConfigStore store;
	private final Map<String, HudModuleSettings> hudModuleSettings = new HashMap<>();

	CombatLabOptions(CombatLabConfig config, ConfigStore store) {
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

	public HudModuleSettings bindHudModule(String id, double defaultX, double defaultY) {
		return hudModuleSettings.computeIfAbsent(id, ignored -> {
			HudModuleConfig module = config.hudModules.get(id);
			if (module == null) {
				module = new HudModuleConfig();
				module.normalizedX = clampPosition(defaultX);
				module.normalizedY = clampPosition(defaultY);
				config.hudModules.put(id, module);
			}
			return new HudModuleSettings(module, this::save);
		});
	}

	public void save() {
		store.save(config);
	}

	private static double clampPosition(double value) {
		return Math.clamp(value, 0.0, 1.0);
	}
}
