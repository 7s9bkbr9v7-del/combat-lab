package dev.combatlab.client.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class CombatLabConfigCodecTest {
  private final CombatLabConfigCodec codec = new CombatLabConfigCodec();

  @Test
  void migratesTheLegacyFlatConfiguration() {
    CombatLabConfig config =
        codec.decode(
            """
				{
				  "combatHudEnabled": true,
				  "debugLoggingEnabled": true,
				  "combatHudX": 960,
				  "combatHudY": 540
				}
				""");

    assertEquals(CombatLabConfig.CURRENT_SCHEMA_VERSION, config.schemaVersion);
    assertTrue(config.debugLoggingEnabled);
    assertTrue(config.dynamicFovEnabled);
    assertTrue(config.hudModules.isEmpty());
  }

  @Test
  void roundTripsTheCurrentSchema() {
    CombatLabConfig original = new CombatLabConfig();
    original.fullbrightEnabled = true;
    original.achievementToastsDisabled = true;
    original.dynamicFovEnabled = false;
    HudModuleConfig module = new HudModuleConfig();
    module.enabled = false;
    module.normalizedX = 0.2;
    module.normalizedY = 0.8;
    module.scale = 1.75;
    module.layout = "VERTICAL";
    module.attachedTo = "combatlab:cps";
    module.attachmentSide = "BELOW";
    module.attachmentOffset = 4;
    original.hudModules.put("combatlab:fps", module);

    CombatLabConfig decoded = codec.decode(codec.encode(original));
    HudModuleConfig roundTripped = decoded.hudModules.get("combatlab:fps");
    assertTrue(decoded.fullbrightEnabled);
    assertTrue(decoded.achievementToastsDisabled);
    assertFalse(decoded.dynamicFovEnabled);
    assertFalse(roundTripped.enabled);
    assertEquals(0.2, roundTripped.normalizedX, 0.0001);
    assertEquals(0.8, roundTripped.normalizedY, 0.0001);
    assertEquals(1.75, roundTripped.scale, 0.0001);
    assertEquals("VERTICAL", roundTripped.layout);
    assertEquals("combatlab:cps", roundTripped.attachedTo);
    assertEquals("BELOW", roundTripped.attachmentSide);
    assertEquals(4, roundTripped.attachmentOffset);
  }

  @Test
  void defaultsMissingModuleScale() {
    CombatLabConfig decoded =
        codec.decode(
            """
				{
				  "schemaVersion": 2,
				  "hudModules": {
				    "combatlab:fps": {
				      "enabled": true,
				      "normalizedX": 0.1,
				      "normalizedY": 0.2
				    }
				  }
				}
				""");

    assertEquals(1.0, decoded.hudModules.get("combatlab:fps").scale, 0.0001);
    assertTrue(decoded.dynamicFovEnabled);
  }

  @Test
  void defaultsDynamicFovOnWhenMigratingSchemaTwo() {
    CombatLabConfig decoded =
        codec.decode(
            """
				{
				  "schemaVersion": 2,
				  "hudModules": {}
				}
				""");

    assertEquals(CombatLabConfig.CURRENT_SCHEMA_VERSION, decoded.schemaVersion);
    assertTrue(decoded.dynamicFovEnabled);
  }

  @Test
  void rejectsInvalidRootJsonWithClearErrors() {
    IllegalArgumentException malformed =
        assertThrows(IllegalArgumentException.class, () -> codec.decode("{"));
    assertEquals("Config must be valid JSON", malformed.getMessage());

    IllegalArgumentException wrongRoot =
        assertThrows(IllegalArgumentException.class, () -> codec.decode("[]"));
    assertEquals("Config root must be a JSON object", wrongRoot.getMessage());
  }

  @Test
  void rejectsWrongFieldTypesWithClearErrors() {
    IllegalArgumentException rootField =
        assertThrows(
            IllegalArgumentException.class,
            () -> codec.decode("{\"schemaVersion\":3,\"debugLoggingEnabled\":\"yes\"}"));
    assertEquals("Config field 'debugLoggingEnabled' must be a boolean", rootField.getMessage());

    IllegalArgumentException moduleField =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                codec.decode(
                    """
						{
						  "schemaVersion": 3,
						  "hudModules": {
						    "combatlab:fps": {
						      "normalizedX": "left"
						    }
						  }
						}
						"""));
    assertEquals(
        "HUD module 'combatlab:fps' field 'normalizedX' must be a finite number",
        moduleField.getMessage());
  }

  @Test
  void normalizesRecoverableModuleValues() {
    CombatLabConfig decoded =
        codec.decode(
            """
				{
				  "schemaVersion": 3,
				  "hudModules": {
				    "combatlab:fps": {
				      "normalizedX": -0.5,
				      "normalizedY": 1.5,
				      "scale": 99.0,
				      "layout": "SIDEWAYS",
				      "attachedTo": "combatlab:cps",
				      "attachmentSide": "NEAR",
				      "attachmentOffset": 12
				    }
				  }
				}
				""");

    HudModuleConfig module = decoded.hudModules.get("combatlab:fps");
    assertEquals(0.0, module.normalizedX, 0.0001);
    assertEquals(1.0, module.normalizedY, 0.0001);
    assertEquals(HudModuleSettings.MAX_SCALE, module.scale, 0.0001);
    assertNull(module.layout);
    assertNull(module.attachedTo);
    assertNull(module.attachmentSide);
    assertEquals(0, module.attachmentOffset);
  }

  @Test
  void rejectsFutureSchemasRatherThanDowngradingThem() {
    assertThrows(IllegalArgumentException.class, () -> codec.decode("{\"schemaVersion\":999}"));
  }
}
