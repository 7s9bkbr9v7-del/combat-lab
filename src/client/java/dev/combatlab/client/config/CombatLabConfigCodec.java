package dev.combatlab.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class CombatLabConfigCodec {
  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
  private static final Set<String> VALID_LAYOUTS =
      Set.of("VERTICAL", "HORIZONTAL", "GRID", "FLOATING", "SIDE");
  private static final Set<String> VALID_ATTACHMENT_SIDES =
      Set.of("LEFT_OF", "RIGHT_OF", "ABOVE", "BELOW");

  public CombatLabConfig decode(String json) {
    JsonObject root = parseRoot(json);
    if (root.has("schemaVersion")) {
      int schemaVersion = schemaVersion(root);
      if (schemaVersion > CombatLabConfig.CURRENT_SCHEMA_VERSION) {
        throw new IllegalArgumentException("Unsupported future config schema: " + schemaVersion);
      }
      if (schemaVersion < 2) {
        throw new IllegalArgumentException("Unsupported config schema: " + schemaVersion);
      }
      CombatLabConfig decoded = decodeVersioned(root, schemaVersion);
      decoded.schemaVersion = CombatLabConfig.CURRENT_SCHEMA_VERSION;
      return decoded;
    }

    return migrateLegacy(root);
  }

  public String encode(CombatLabConfig config) {
    return GSON.toJson(config);
  }

  private static JsonObject parseRoot(String json) {
    JsonElement parsed;
    try {
      parsed = JsonParser.parseString(json);
    } catch (JsonParseException exception) {
      throw new IllegalArgumentException("Config must be valid JSON", exception);
    }
    if (!parsed.isJsonObject()) {
      throw new IllegalArgumentException("Config root must be a JSON object");
    }
    return parsed.getAsJsonObject();
  }

  private static CombatLabConfig decodeVersioned(JsonObject root, int schemaVersion) {
    CombatLabConfig decoded = new CombatLabConfig();
    decoded.debugLoggingEnabled = optionalBoolean(root, "debugLoggingEnabled", false);
    decoded.fullbrightEnabled = optionalBoolean(root, "fullbrightEnabled", false);
    decoded.achievementToastsDisabled = optionalBoolean(root, "achievementToastsDisabled", false);
    boolean storedDynamicFovEnabled = optionalBoolean(root, "dynamicFovEnabled", true);
    decoded.dynamicFovEnabled = schemaVersion < 3 || storedDynamicFovEnabled;
    decoded.hudModules = decodeHudModules(root);
    return decoded;
  }

  private static CombatLabConfig migrateLegacy(JsonObject root) {
    CombatLabConfig migrated = new CombatLabConfig();
    migrated.debugLoggingEnabled = optionalBoolean(root, "debugLoggingEnabled", false);
    migrated.fullbrightEnabled = optionalBoolean(root, "fullbrightEnabled", false);
    migrated.achievementToastsDisabled = optionalBoolean(root, "achievementToastsDisabled", false);
    migrated.dynamicFovEnabled = optionalBoolean(root, "dynamicFovEnabled", true);
    return migrated;
  }

  private static Map<String, HudModuleConfig> decodeHudModules(JsonObject root) {
    if (!root.has("hudModules") || root.get("hudModules").isJsonNull()) {
      return new HashMap<>();
    }
    JsonElement modulesElement = root.get("hudModules");
    if (!modulesElement.isJsonObject()) {
      throw new IllegalArgumentException("Config field 'hudModules' must be an object");
    }

    Map<String, HudModuleConfig> modules = new HashMap<>();
    JsonObject moduleObjects = modulesElement.getAsJsonObject();
    for (Map.Entry<String, JsonElement> entry : moduleObjects.entrySet()) {
      if (!entry.getValue().isJsonObject()) {
        throw new IllegalArgumentException("HUD module '" + entry.getKey() + "' must be an object");
      }
      modules.put(
          entry.getKey(), decodeHudModule(entry.getKey(), entry.getValue().getAsJsonObject()));
    }
    return modules;
  }

  private static HudModuleConfig decodeHudModule(String id, JsonObject moduleObject) {
    HudModuleConfig module = new HudModuleConfig();
    module.enabled = optionalBoolean(moduleObject, "enabled", false, "HUD module '" + id + "'");
    module.normalizedX =
        optionalNormalizedPosition(moduleObject, "normalizedX", "HUD module '" + id + "'");
    module.normalizedY =
        optionalNormalizedPosition(moduleObject, "normalizedY", "HUD module '" + id + "'");
    module.scale = optionalScale(moduleObject, id);
    module.layout =
        optionalAllowedString(moduleObject, "layout", VALID_LAYOUTS, "HUD module '" + id + "'");
    module.attachedTo = optionalString(moduleObject, "attachedTo", "HUD module '" + id + "'");
    module.attachmentSide =
        optionalAllowedString(
            moduleObject, "attachmentSide", VALID_ATTACHMENT_SIDES, "HUD module '" + id + "'");
    module.attachmentOffset = optionalAttachmentOffset(moduleObject, "HUD module '" + id + "'");
    if (module.attachedTo == null || module.attachmentSide == null) {
      module.attachedTo = null;
      module.attachmentSide = null;
      module.attachmentOffset = 0;
    }
    return module;
  }

  private static double optionalScale(JsonObject root, String id) {
    if (!root.has("scale") || root.get("scale").isJsonNull()) {
      return 1.0;
    }
    double scale = doubleValue(root, "scale", "HUD module '" + id + "'");
    if (scale <= 0.0) {
      return 1.0;
    }
    return Math.clamp(scale, HudModuleSettings.MIN_SCALE, HudModuleSettings.MAX_SCALE);
  }

  private static boolean optionalBoolean(JsonObject root, String name, boolean fallback) {
    return optionalBoolean(root, name, fallback, "Config");
  }

  private static boolean optionalBoolean(
      JsonObject root, String name, boolean fallback, String owner) {
    if (!root.has(name) || root.get(name).isJsonNull()) {
      return fallback;
    }
    JsonElement value = root.get(name);
    if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isBoolean()) {
      throw new IllegalArgumentException(owner + " field '" + name + "' must be a boolean");
    }
    return value.getAsBoolean();
  }

  private static double optionalNormalizedPosition(JsonObject root, String name, String owner) {
    if (!root.has(name) || root.get(name).isJsonNull()) {
      return 0.0;
    }
    return Math.clamp(doubleValue(root, name, owner), 0.0, 1.0);
  }

  private static double doubleValue(JsonObject root, String name, String owner) {
    JsonElement value = root.get(name);
    if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isNumber()) {
      throw new IllegalArgumentException(owner + " field '" + name + "' must be a finite number");
    }
    double number = value.getAsDouble();
    if (!Double.isFinite(number)) {
      throw new IllegalArgumentException(owner + " field '" + name + "' must be a finite number");
    }
    return number;
  }

  private static int schemaVersion(JsonObject root) {
    return intValue(root, "schemaVersion", "Config");
  }

  private static int optionalAttachmentOffset(JsonObject root, String owner) {
    if (!root.has("attachmentOffset") || root.get("attachmentOffset").isJsonNull()) {
      return 0;
    }
    return intValue(root, "attachmentOffset", owner);
  }

  private static int intValue(JsonObject root, String name, String owner) {
    JsonElement value = root.get(name);
    if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isNumber()) {
      throw new IllegalArgumentException(owner + " field '" + name + "' must be an integer");
    }
    try {
      BigDecimal number = value.getAsBigDecimal().stripTrailingZeros();
      if (number.scale() > 0) {
        throw new ArithmeticException();
      }
      return number.intValueExact();
    } catch (ArithmeticException | NumberFormatException exception) {
      throw new IllegalArgumentException(
          owner + " field '" + name + "' must be an integer", exception);
    }
  }

  private static String optionalString(JsonObject root, String name, String owner) {
    if (!root.has(name) || root.get(name).isJsonNull()) {
      return null;
    }
    JsonElement value = root.get(name);
    if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString()) {
      throw new IllegalArgumentException(owner + " field '" + name + "' must be a string");
    }
    String text = value.getAsString();
    if (text.isBlank()) {
      return null;
    }
    return text;
  }

  private static String optionalAllowedString(
      JsonObject root, String name, Set<String> allowedValues, String owner) {
    String text = optionalString(root, name, owner);
    if (text == null || allowedValues.contains(text)) {
      return text;
    }
    return null;
  }
}
