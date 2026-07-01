package dev.combatlab.client.hud;

import dev.combatlab.client.config.CombatLabOptions;
import dev.combatlab.client.debug.DebugLogger;
import dev.combatlab.client.state.ClientGameState;
import dev.combatlab.client.state.MovementState;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public final class MovementStatusHud extends TextHudModule {
  private static final HudModuleDefinition DEFINITION =
      new HudModuleDefinition(
          Identifier.fromNamespaceAndPath("combatlab", "movement_status"),
          Component.literal("Movement status HUD"),
          1.0,
          0.14,
          true);

  public static HudModuleDescriptor descriptor() {
    return new HudModuleDescriptor(
        DEFINITION,
        dependencies -> new MovementStatusHud(dependencies.options(), dependencies.debug()));
  }

  private boolean active;

  public MovementStatusHud(CombatLabOptions options, DebugLogger debug) {
    super(DEFINITION, "Movement status", options, debug);
  }

  @Override
  public void tick(ClientGameState gameState) {
    MovementState movement = HudGameState.from(gameState).movement();
    String status =
        MovementStatusText.resolve(
            movement.crouching(), movement.sprinting(), movement.sprintToggled());
    active = !status.isEmpty();
    setText(active ? status : "Movement status");
  }

  @Override
  protected boolean shouldRenderInGame(HudRenderContext context) {
    return active;
  }
}
