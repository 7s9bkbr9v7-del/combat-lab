package dev.combatlab.client.external;

import java.util.List;

public record ExternalCombatLabSettingsDocument(
    int schemaVersion,
    boolean debugLoggingEnabled,
    boolean fullbrightEnabled,
    boolean achievementToastsDisabled,
    boolean dynamicFovEnabled,
    List<ExternalHudModuleSettings> hudModules) {}
