package dev.combatlab.client.config;

import java.util.HashMap;
import java.util.Map;

public final class CombatLabConfig {
	public static final int CURRENT_SCHEMA_VERSION = 2;

	int schemaVersion = CURRENT_SCHEMA_VERSION;
	boolean debugLoggingEnabled;
	boolean fullbrightEnabled;
	boolean achievementToastsDisabled;
	Map<String, HudModuleConfig> hudModules = new HashMap<>();
}
