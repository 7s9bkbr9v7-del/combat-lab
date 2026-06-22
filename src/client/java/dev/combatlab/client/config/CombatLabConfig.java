package dev.combatlab.client.config;

import java.util.HashMap;
import java.util.Map;

public final class CombatLabConfig {
	public static final int CURRENT_SCHEMA_VERSION = 3;

	int schemaVersion = CURRENT_SCHEMA_VERSION;
	boolean debugLoggingEnabled;
	boolean fullbrightEnabled;
	boolean achievementToastsDisabled;
	boolean dynamicFovEnabled = true;
	Map<String, HudModuleConfig> hudModules = new HashMap<>();
}
