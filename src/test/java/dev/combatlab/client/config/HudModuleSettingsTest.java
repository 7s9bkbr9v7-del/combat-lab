package dev.combatlab.client.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class HudModuleSettingsTest {
  @TempDir Path temporaryDirectory;

  @Test
  void bindsEachModuleToOneStableSettingsObject() {
    CombatLabOptions options = options();

    HudModuleSettings first = options.bindHudModule("combatlab:fps", 0.25, 0.75);
    HudModuleSettings second = options.bindHudModule("combatlab:fps", 0.9, 0.1);

    assertSame(first, second);
    assertEquals(0.25, first.normalizedX(), 0.0001);
    assertEquals(0.75, first.normalizedY(), 0.0001);
    assertEquals(HudModuleSettings.DEFAULT_SCALE, first.scale(), 0.0001);
  }

  @Test
  void clampsMutableLayoutValuesAtTheBindingBoundary() {
    HudModuleSettings settings = options().bindHudModule("combatlab:fps", 0.25, 0.75);

    settings.updatePosition(-1.0, 2.0);
    settings.updateScale(10.0);

    assertEquals(0.0, settings.normalizedX(), 0.0001);
    assertEquals(1.0, settings.normalizedY(), 0.0001);
    assertEquals(HudModuleSettings.MAX_SCALE, settings.scale(), 0.0001);
  }

  @Test
  void displaysDefaultScaleAsOneHundredPercent() {
    assertEquals(100, HudModuleSettings.displayPercent(HudModuleSettings.DEFAULT_SCALE));
    assertEquals(200, HudModuleSettings.displayPercent(HudModuleSettings.DEFAULT_SCALE * 2.0));
  }

  private CombatLabOptions options() {
    ConfigStore store =
        new ConfigStore(temporaryDirectory.resolve("combatlab.json"), new CombatLabConfigCodec());
    return new CombatLabOptions(new CombatLabConfig(), store);
  }
}
