package dev.combatlab.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public final class CombatLabConfigCodec {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	public CombatLabConfig decode(String json) {
		JsonObject root = JsonParser.parseString(json).getAsJsonObject();
		if (root.has("schemaVersion")) {
			int schemaVersion = root.get("schemaVersion").getAsInt();
			if (schemaVersion > CombatLabConfig.CURRENT_SCHEMA_VERSION) {
				throw new IllegalArgumentException("Unsupported future config schema: " + schemaVersion);
			}
			if (schemaVersion < 2) {
				throw new IllegalArgumentException("Unsupported config schema: " + schemaVersion);
			}
			CombatLabConfig decoded = GSON.fromJson(root, CombatLabConfig.class);
			if (schemaVersion < 3 || !root.has("dynamicFovEnabled")) {
				decoded.dynamicFovEnabled = true;
			}
			if (decoded.hudModules == null) {
				decoded.hudModules = new java.util.HashMap<>();
			}
			for (HudModuleConfig module : decoded.hudModules.values()) {
				if (module.scale <= 0.0) {
					module.scale = 1.0;
				}
			}
			decoded.schemaVersion = CombatLabConfig.CURRENT_SCHEMA_VERSION;
			return decoded;
		}

		CombatLabConfig migrated = new CombatLabConfig();
		migrated.debugLoggingEnabled = booleanValue(root, "debugLoggingEnabled", false);
		migrated.fullbrightEnabled = booleanValue(root, "fullbrightEnabled", false);
		migrated.achievementToastsDisabled = booleanValue(root, "achievementToastsDisabled", false);
		migrated.dynamicFovEnabled = booleanValue(root, "dynamicFovEnabled", true);
		return migrated;
	}

	public String encode(CombatLabConfig config) {
		return GSON.toJson(config);
	}

	private static boolean booleanValue(JsonObject root, String name, boolean fallback) {
		return root.has(name) ? root.get(name).getAsBoolean() : fallback;
	}

}
