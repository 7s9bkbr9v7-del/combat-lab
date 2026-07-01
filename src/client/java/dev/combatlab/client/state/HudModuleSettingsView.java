package dev.combatlab.client.state;

public record HudModuleSettingsView(
    String id,
    String displayName,
    boolean enabled,
    double normalizedX,
    double normalizedY,
    double scale,
    String layout,
    String attachedTo,
    String attachmentSide,
    int attachmentOffset) {}
