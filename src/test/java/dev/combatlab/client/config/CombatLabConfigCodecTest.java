package dev.combatlab.client.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CombatLabConfigCodecTest {
	private final CombatLabConfigCodec codec = new CombatLabConfigCodec();

	@Test
	void migratesTheLegacyFlatConfiguration() {
		CombatLabConfig config = codec.decode("""
				{
				  "combatHudEnabled": true,
				  "debugLoggingEnabled": true,
				  "combatHudX": 960,
				  "combatHudY": 540
				}
				""");

		assertEquals(CombatLabConfig.CURRENT_SCHEMA_VERSION, config.schemaVersion);
		assertTrue(config.debugLoggingEnabled);
		assertTrue(config.hudModules.isEmpty());
	}

	@Test
	void roundTripsTheCurrentSchema() {
		CombatLabConfig original = new CombatLabConfig();
		original.fullbrightEnabled = true;
		original.achievementToastsDisabled = true;
		HudModuleConfig module = new HudModuleConfig();
		module.enabled = false;
		module.normalizedX = 0.2;
		module.normalizedY = 0.8;
		original.hudModules.put("combatlab:fps", module);

		CombatLabConfig decoded = codec.decode(codec.encode(original));
		HudModuleConfig roundTripped = decoded.hudModules.get("combatlab:fps");
		assertTrue(decoded.fullbrightEnabled);
		assertTrue(decoded.achievementToastsDisabled);
		assertFalse(roundTripped.enabled);
		assertEquals(0.2, roundTripped.normalizedX, 0.0001);
		assertEquals(0.8, roundTripped.normalizedY, 0.0001);
	}

	@Test
	void rejectsFutureSchemasRatherThanDowngradingThem() {
		assertThrows(IllegalArgumentException.class, () -> codec.decode("{\"schemaVersion\":999}"));
	}
}
