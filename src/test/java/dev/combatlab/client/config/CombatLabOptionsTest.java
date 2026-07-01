package dev.combatlab.client.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CombatLabOptionsTest {
  @TempDir Path temporaryDirectory;

  @Test
  void publishesTypedOptionChangesAfterSaving() {
    CombatLabOptions options = options();
    List<String> changes = new ArrayList<>();
    options.addChangeListener(
        new CombatLabOptionsChangeListener() {
          @Override
          public void onDebugLoggingEnabledChanged(boolean enabled) {
            changes.add("debug=" + enabled);
          }

          @Override
          public void onFullbrightEnabledChanged(boolean enabled) {
            changes.add("fullbright=" + enabled);
          }

          @Override
          public void onAchievementToastsDisabledChanged(boolean disabled) {
            changes.add("toastsDisabled=" + disabled);
          }

          @Override
          public void onDynamicFovEnabledChanged(boolean enabled) {
            changes.add("dynamicFov=" + enabled);
          }
        });

    options.setDebugLoggingEnabled(true);
    options.setFullbrightEnabled(true);
    options.setAchievementToastsDisabled(true);
    options.setDynamicFovEnabled(false);

    assertEquals(
        List.of("debug=true", "fullbright=true", "toastsDisabled=true", "dynamicFov=false"),
        changes);
  }

  private CombatLabOptions options() {
    ConfigStore store =
        new ConfigStore(temporaryDirectory.resolve("combatlab.json"), new CombatLabConfigCodec());
    return new CombatLabOptions(new CombatLabConfig(), store);
  }
}
