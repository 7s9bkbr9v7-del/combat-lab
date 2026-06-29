package dev.combatlab.client.model;

import java.util.UUID;

public record AttackSample(
    long gameTick,
    long capturedAtNanos,
    UUID targetId,
    String targetName,
    float targetDistance,
    float attackStrength,
    int ping) {}
