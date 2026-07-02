package dev.combatlab.client.external;

import dev.combatlab.client.config.CombatLabOptions;
import dev.combatlab.client.state.ClientGameState;
import dev.combatlab.client.state.HudModuleCatalog;
import dev.combatlab.client.state.HudModuleSettingsView;
import dev.combatlab.client.state.PlayerEffectTimer;
import dev.combatlab.client.state.PlayerEffects;
import dev.combatlab.client.state.TargetState;
import java.util.List;

public final class CombatLabExternalData {
  private CombatLabExternalData() {}

  public static ExternalCombatLabSettingsDocument settingsDocument(
      CombatLabOptions options, List<HudModuleSettingsView> modules) {
    return new ExternalCombatLabSettingsDocument(
        CombatLabExternalSchema.SETTINGS_SCHEMA_VERSION,
        options.debugLoggingEnabled(),
        options.fullbrightEnabled(),
        options.achievementToastsDisabled(),
        options.dynamicFovEnabled(),
        moduleSettings(modules));
  }

  public static ExternalHudModuleManifest moduleManifest(HudModuleCatalog modules) {
    return new ExternalHudModuleManifest(
        CombatLabExternalSchema.MODULE_MANIFEST_SCHEMA_VERSION,
        modules.modules().stream().map(CombatLabExternalData::moduleDefinition).toList());
  }

  public static ExternalTelemetrySnapshot telemetrySnapshot(ClientGameState state) {
    return telemetrySnapshot(state, PlayerEffects.empty());
  }

  public static ExternalTelemetrySnapshot telemetrySnapshot(
      ClientGameState state, PlayerEffects effects) {
    PlayerEffects safeEffects = effects == null ? PlayerEffects.empty() : effects;
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
        safeEffects.active().stream().map(CombatLabExternalData::effect).toList());
  }

  private static List<ExternalHudModuleSettings> moduleSettings(
      List<HudModuleSettingsView> modules) {
    return modules.stream().map(CombatLabExternalData::moduleSettings).toList();
  }

  private static ExternalHudModuleSettings moduleSettings(HudModuleSettingsView settings) {
    return new ExternalHudModuleSettings(
        settings.id(),
        settings.displayName(),
        settings.enabled(),
        settings.normalizedX(),
        settings.normalizedY(),
        settings.scale(),
        settings.textColor(),
        settings.layout(),
        settings.attachedTo(),
        settings.attachmentSide(),
        settings.attachmentOffset());
  }

  private static ExternalHudModuleDefinition moduleDefinition(HudModuleCatalog.Module module) {
    return new ExternalHudModuleDefinition(
        module.id(),
        module.displayName(),
        module.defaultX(),
        module.defaultY(),
        module.resizable(),
        module.loadWhenDisabled());
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
