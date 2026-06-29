package dev.combatlab.client.hud;

/**
 * Describes which parts of a HUD module face inward toward the screen center. Future modules can
 * use this to mirror internal layout without knowing editor behavior; for example, an armor HUD can
 * place durability text on {@link #horizontalSideFacingCenter()}.
 */
public record HudOrientation(
    HudHorizontalSide horizontalSideFacingCenter,
    HudVerticalSide verticalSideFacingCenter,
    HudCorner cornerFacingCenter) {}
