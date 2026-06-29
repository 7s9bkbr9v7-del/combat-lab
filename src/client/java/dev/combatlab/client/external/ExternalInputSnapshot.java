package dev.combatlab.client.external;

public record ExternalInputSnapshot(
    boolean forward,
    boolean left,
    boolean back,
    boolean right,
    boolean jump,
    boolean sneak,
    boolean sprint,
    boolean attack,
    boolean use) {}
