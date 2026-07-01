package dev.combatlab.client.external;

import dev.combatlab.client.config.CombatLabOptions;
import dev.combatlab.client.config.HudModuleSettings;
import dev.combatlab.client.hud.HudGameState;
import dev.combatlab.client.hud.HudModuleDescriptor;
import dev.combatlab.client.hud.HudModuleRegistry;
import dev.combatlab.client.state.ClientGameState;
import dev.combatlab.client.state.PlayerEffectTimer;
import dev.combatlab.client.state.TargetState;
import java.util.List;

public final class CombatLabExternalData {
  private CombatLabExternalData() {}

  public static ExternalCombatLabSettingsDocument settingsDocument(
      CombatLabOptions options, HudModuleRegistry modules) {
    return new ExternalCombatLabSettingsDocument(
        CombatLabExternalSchema.SETTINGS_SCHEMA_VERSION,
        options.debugLoggingEnabled(),
        options.fullbrightEnabled(),
        options.achievementToastsDisabled(),
        options.dynamicFovEnabled(),
        moduleSettings(modules));
  }

  public static ExternalHudModuleManifest moduleManifest(HudModuleRegistry modules) {
    return new ExternalHudModuleManifest(
        CombatLabExternalSchema.MODULE_MANIFEST_SCHEMA_VERSION,
        modules.descriptors().stream().map(CombatLabExternalData::moduleDefinition).toList());
  }

  public static ExternalTelemetrySnapshot telemetrySnapshot(ClientGameState state) {
    return telemetrySnapshot(state, HudGameState.empty());
  }

  public static ExternalTelemetrySnapshot telemetrySnapshot(
      ClientGameState state, HudGameState hud) {
    HudGameState safeHud = hud == null ? HudGameState.empty() : hud;
    return new ExternalTelemetrySnapshot(
        CombatLabExternalSchema.TELEMETRY_SCHEMA_VERSION,
        state.fps(),
        state.input().cps(),
        state.combat().ping(),
        state.combat().attackStrength(),
        new ExternalInputSnapshot(
            state.input().forward(),
            state.input().left(),
            state.input().back(),
            state.input().right(),
            state.input().jump(),
            state.input().sneak(),
            state.input().sprint(),
            state.input().attack(),
            state.input().use()),
        target(state.combat().target()),
        safeHud.effects().active().stream().map(CombatLabExternalData::effect).toList());
  }

  private static List<ExternalHudModuleSettings> moduleSettings(HudModuleRegistry modules) {
    return modules.descriptors().stream()
        .map(descriptor -> moduleSettings(modules, descriptor))
        .toList();
  }

  private static ExternalHudModuleSettings moduleSettings(
      HudModuleRegistry modules, HudModuleDescriptor descriptor) {
    HudModuleSettings settings = modules.settings(descriptor.id());
    return new ExternalHudModuleSettings(
        descriptor.id(),
        descriptor.definition().displayName().getString(),
        settings.enabled(),
        settings.normalizedX(),
        settings.normalizedY(),
        settings.scale(),
        settings.layout(),
        settings.attachedTo(),
        settings.attachmentSide(),
        settings.attachmentOffset());
  }

  private static ExternalHudModuleDefinition moduleDefinition(HudModuleDescriptor descriptor) {
    return new ExternalHudModuleDefinition(
        descriptor.id(),
        descriptor.definition().displayName().getString(),
        descriptor.definition().defaultX(),
        descriptor.definition().defaultY(),
        descriptor.definition().resizable(),
        descriptor.loadWhenDisabled());
  }

  private static ExternalTargetSnapshot target(TargetState target) {
    return new ExternalTargetSnapshot(
        target.present(),
        target.id() == null ? null : target.id().toString(),
        target.name(),
        target.distance());
  }

  private static ExternalEffectTimer effect(PlayerEffectTimer effect) {
    return new ExternalEffectTimer(
        effect.id(),
        effect.displayName(),
        effect.amplifier(),
        effect.durationTicks(),
        effect.infinite(),
        effect.ambient(),
        effect.color());
  }
}
