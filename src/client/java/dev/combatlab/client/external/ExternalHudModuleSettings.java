package dev.combatlab.client.external;

public record ExternalHudModuleSettings(
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
