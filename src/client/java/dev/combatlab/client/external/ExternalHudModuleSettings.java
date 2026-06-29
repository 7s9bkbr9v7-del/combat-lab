package dev.combatlab.client.external;

public record ExternalHudModuleSettings(
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
