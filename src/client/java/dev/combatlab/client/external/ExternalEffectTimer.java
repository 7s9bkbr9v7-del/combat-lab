package dev.combatlab.client.external;

public record ExternalEffectTimer(
    String id,
    String displayName,
    int amplifier,
    int durationTicks,
    boolean infinite,
    boolean ambient,
    int color) {}
