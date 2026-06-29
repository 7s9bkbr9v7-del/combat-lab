package dev.combatlab.client.hud;

import java.util.List;

public record HudOutlineSegments(
    List<HudOutlineSegment> top,
    List<HudOutlineSegment> right,
    List<HudOutlineSegment> bottom,
    List<HudOutlineSegment> left) {}
