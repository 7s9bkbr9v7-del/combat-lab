package dev.combatlab.client.state;

public record HudModuleSettingsView(
    String id,
    String displayName,
    boolean enabled,
    double normalizedX,
    double normalizedY,
    double scale,
    int textColor,
    String layout,
    String attachedTo,
    String attachmentSide,
    int attachmentOffset) {}
